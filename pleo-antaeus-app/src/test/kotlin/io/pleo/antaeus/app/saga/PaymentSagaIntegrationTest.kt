package io.pleo.antaeus.app.saga

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.app.AntaeusAppTest
import io.pleo.antaeus.app.payment.PaymentCommandHandler
import io.pleo.antaeus.app.payment.PaymentEventHandler
import io.pleo.antaeus.app.verticle.PaymentVerticle
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.messagebus.VertxCommandBus
import io.pleo.antaeus.messagebus.VertxEventBus
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
class PaymentSagaIntegrationTest {

    lateinit var dal: AntaeusDal
    lateinit var db: Database
    lateinit var paymentSaga: PaymentSaga
    private val paymentsMap = mutableMapOf<UUID, Payment?>()
    private val paymentProvider = mockk<PaymentProvider>(relaxed = true)
    lateinit var paymentCommandHandler: PaymentCommandHandler
    lateinit var paymentEventHandler: PaymentEventHandler
    lateinit var invoiceService: InvoiceService
    lateinit var paymentService: PaymentService
    lateinit var commandBus: CommandBus
    lateinit var eventBus: EventBus

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
        paymentsMap.clear()
        dal = AntaeusDal(db = db)
        val invoiceRepository = ExposedInvoiceRepository(db)
        val paymentRepository = InMemoryPaymentRepository(paymentsMap)

        commandBus = VertxCommandBus(vertx.eventBus())
        eventBus = VertxEventBus(vertx.eventBus())
        invoiceService = InvoiceService(invoiceRepository, paymentProvider)
        paymentService = PaymentService(invoiceRepository, paymentRepository, commandBus)
        paymentSaga = PaymentSaga(commandBus)

        paymentCommandHandler = PaymentCommandHandler(paymentService, invoiceService)
        paymentEventHandler = PaymentEventHandler(paymentSaga)
        vertx.deployVerticle(PaymentVerticle(
                paymentCommandHandler = paymentCommandHandler,
                paymentEventHandler = paymentEventHandler
        ), testContext.completing())
    }

    @Test
    fun `should cancel payment when invoice does not exist`(vertx: Vertx, testContext: VertxTestContext) {
        //When request create payment
        commandBus.send(CreatePaymentCommand(100))
        testContext.awaitCompletion(100, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        val payment = paymentsMap.values.first()
        assert(payment!!.status == PaymentStatus.CANCELED)
        assert(payment!!.cancellationReason == "InvoiceNotFoundException")
    }

    @Test
    fun `should cancel payment when invoice is paid`(vertx: Vertx, testContext: VertxTestContext) {
        //Given a paid invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PAID)
        //When request create payment
        commandBus.send(CreatePaymentCommand(invoice!!.id))
        testContext.awaitCompletion(100, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        val payment = paymentsMap.values.first()
        val invoiceResult = dal.fetchInvoice(invoice.id)
        assert(invoiceResult == invoice)
        assert(payment!!.status == PaymentStatus.CANCELED)
        assert(payment!!.cancellationReason == "InvalidInvoiceStatusException")
    }

    @Test
    fun `should cancel payment when charge fails`(vertx: Vertx, testContext: VertxTestContext) {
        //Given a paid invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)

        every { paymentProvider.charge(any()) } throws NetworkException()

        //When request create payment
        commandBus.send(CreatePaymentCommand(invoice!!.id))
        testContext.awaitCompletion(100, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        val payment = paymentsMap.values.first()
        val invoiceResult = dal.fetchInvoice(invoice.id)

        assert(invoiceResult == invoice.copy(version = 3))
        assert(payment!!.status == PaymentStatus.CANCELED)
        assert(payment!!.cancellationReason == "NetworkException")
    }

    @Test
    fun `should cancel payment when charge false`(vertx: Vertx, testContext: VertxTestContext) {
        //Given a paid invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)

        every { paymentProvider.charge(any()) } returns false

        //When request create payment
        commandBus.send(CreatePaymentCommand(invoice!!.id))
        testContext.awaitCompletion(100, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        val payment = paymentsMap.values.first()
        val invoiceResult = dal.fetchInvoice(invoice.id)

        assert(invoiceResult == invoice.copy(version = 3))
        assert(payment!!.status == PaymentStatus.CANCELED)
        assert(payment!!.cancellationReason == "AccountBalanceException")
    }

    @Test
    fun `should complete payment when charge true`(vertx: Vertx, testContext: VertxTestContext) {
        //Given a paid invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                AntaeusAppTest.TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)

        every { paymentProvider.charge(any()) } returns true

        //When request create payment
        commandBus.send(CreatePaymentCommand(invoice!!.id))
        testContext.awaitCompletion(100, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        val payment = paymentsMap.values.first()
        val invoiceResult = dal.fetchInvoice(invoice.id)

        assert(invoiceResult == invoice.copy(version = 2, status = InvoiceStatus.PAID))
        assert(payment!!.status == PaymentStatus.COMPLETED)
    }

}