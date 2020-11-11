package io.pleo.antaeus.app.commandhandlers

import getCurrencyExchangeProvider
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.app.AntaeusAppTest
import io.pleo.antaeus.app.PaymentsVerticle
import io.pleo.antaeus.app.saga.PaymentSaga
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.CreatePaymentCommand
import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentCancellationReason
import io.pleo.antaeus.context.payment.PaymentStatus
import io.pleo.antaeus.context.payment.external.CurrencyExchangeProvider
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.messagebus.VertxCommandBus
import io.pleo.antaeus.core.messagebus.VertxEventBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.model.BillingsTable
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.InvoiceTable
import io.pleo.antaeus.model.PaymentTable
import io.pleo.antaeus.repository.ExposedInvoiceRepository
import io.pleo.antaeus.repository.InMemoryPaymentRepository
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.sql.Connection
import java.util.*
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentCommandHandlerTest {

    lateinit var dal: AntaeusDal
    lateinit var db: Database
    lateinit var commandBus: CommandBus
    lateinit var currencyExchangeProvider: CurrencyExchangeProvider
    private val paymentProvider = mockk<PaymentProvider>(relaxed = true)
    private val paymentsMap = mutableMapOf<UUID, Payment?>()
    lateinit var paymentCommandHandler: PaymentCommandHandler
    lateinit var invoiceCommandHandler: InvoiceCommandHandler
    lateinit var paymentSaga: PaymentSaga

    @BeforeAll
    fun init() {
        val tables = arrayOf(BillingsTable, PaymentTable, InvoiceTable, CustomerTable)
        val dbFile: File = File.createTempFile("antaeus-db", ".2")

        db = Database
                .connect(url = "jdbc:sqlite:${dbFile.absolutePath}",
                        driver = "org.sqlite.JDBC",
                        user = "root",
                        password = "")
                .also {
                    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                    transaction(it) {
                        addLogger(StdOutSqlLogger)
                        // Drop all existing tables to ensure a clean slate on each run
                        SchemaUtils.drop(*tables)
                        // Create all tables
                        SchemaUtils.create(*tables)
                    }
                }

    }

    @BeforeEach
    fun setup(vertx: Vertx, testContext: VertxTestContext) {
        dal = AntaeusDal(db = db)
        val invoiceRepository = ExposedInvoiceRepository(db)
        val paymentRepository = InMemoryPaymentRepository(paymentsMap)

        commandBus = VertxCommandBus(vertx.eventBus())
        val eventBus = VertxEventBus(vertx.eventBus())

        currencyExchangeProvider = getCurrencyExchangeProvider()

        paymentCommandHandler = PaymentCommandHandler(
                repository = paymentRepository,
                eventBus = eventBus)
        invoiceCommandHandler = InvoiceCommandHandler(
                repository = invoiceRepository,
                paymentProvider = paymentProvider,
                currencyExchangeProvider = currencyExchangeProvider,
                eventBus = eventBus)

        paymentSaga = PaymentSaga(commandBus = commandBus)


        vertx.deployVerticle(PaymentsVerticle(
                paymentSaga = paymentSaga,
                paymentCommandHandler = paymentCommandHandler,
                invoiceCommandHandler = invoiceCommandHandler
        ), testContext.completing())
    }

    @Test
    fun `should process payment as cancel when invoice fails during payment`(vertx: Vertx, testContext: VertxTestContext) {
        //give a pending invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)
        //payment provider fails
        every { paymentProvider.charge(invoice!!) } throws CustomerNotFoundException(id = aCustomer.id)
        //when request payment
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id))
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
        testContext.completeNow()
        //payment canceled
        assert(dal.fetchInvoice(id = invoice.id)!!.status == InvoiceStatus.PENDING)
        assert(paymentsMap.values.isNotEmpty())
        paymentsMap.values.first()?.let {
            assert(it.status == PaymentStatus.CANCELED)
            assert(it.paymentCancellationReason == PaymentCancellationReason.CUSTOMER_NOT_FOUND)
        }

    }

    @Test
    fun `should process payment as complete when invoice successes during payment`(vertx: Vertx, testContext:
    VertxTestContext) {
        //give a pending invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)
        //payment provider fails
        every { paymentProvider.charge(invoice!!) } returns true
        //when request payment
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id))
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
        testContext.completeNow()
        //payment canceled
        assert(dal.fetchInvoice(id = invoice.id)!!.status == InvoiceStatus.PAID)
        assert(paymentsMap.values.isNotEmpty())
        paymentsMap.values.first()?.let {
            assert(it.status == PaymentStatus.COMPLETED)

        }
    }

    @Test
    fun `should re-process payment as complete when invoice successes during payment`(vertx: Vertx, testContext:
    VertxTestContext) {
        //give a pending invoice
        val aCustomer = dal.createCustomer(Currency.GBP)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)
        //payment provider fails
//        every { paymentProvider.charge(invoice!!) } returns true
        //when request payment
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id))
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
        testContext.completeNow()
        //payment canceled
        assert(dal.fetchInvoice(id = invoice.id)!!.status == InvoiceStatus.PAID)
        assert(paymentsMap.values.isNotEmpty())
        paymentsMap.values.first()?.let {
            assert(it.status == PaymentStatus.COMPLETED)

        }
    }
}