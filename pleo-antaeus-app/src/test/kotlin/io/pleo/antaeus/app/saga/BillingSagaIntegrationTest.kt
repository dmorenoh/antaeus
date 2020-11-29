package io.pleo.antaeus.app.saga

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.app.billing.BillingCommandHandler
import io.pleo.antaeus.app.billing.BillingEventHandler
import io.pleo.antaeus.app.billing.BillingVerticle
import io.pleo.antaeus.app.payment.PaymentCommandHandler
import io.pleo.antaeus.app.payment.PaymentEventHandler
import io.pleo.antaeus.app.verticle.PaymentVerticle
import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentSaga
import io.pleo.antaeus.context.payment.PaymentService
import io.pleo.antaeus.context.payment.PaymentStatus
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.messagebus.VertxCommandBus
import io.pleo.antaeus.messagebus.VertxEventBus
import io.pleo.antaeus.model.BillingsTable
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.InvoiceTable
import io.pleo.antaeus.model.PaymentTable
import io.pleo.antaeus.repository.ExposedInvoiceRepository
import io.pleo.antaeus.repository.InMemoryBillingRepository
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
import java.math.BigDecimal
import java.sql.Connection
import java.util.*
import java.util.concurrent.TimeUnit

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BillingSagaIntegrationTest {

    companion object {
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private lateinit var billingCommandHandler: BillingCommandHandler
    private lateinit var billingEventHandler: BillingEventHandler
    private lateinit var paymentCommandHandler: PaymentCommandHandler
    private lateinit var paymentEventHandler: PaymentEventHandler
    private val paymentProvider = mockk<PaymentProvider>(relaxed = true)
    private val billingMap = mutableMapOf<UUID, Billing?>()
    lateinit var dal: AntaeusDal
    lateinit var db: Database
    private val paymentsMap = mutableMapOf<UUID, Payment?>()
    private lateinit var billingService: BillingService
    private lateinit var billingSaga: BillingSaga
    private lateinit var paymentSaga: PaymentSaga
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

        dal = AntaeusDal(db = db)
        val invoiceRepository = ExposedInvoiceRepository(db)
        val paymentRepository = InMemoryPaymentRepository(paymentsMap)
        val billingRepository = InMemoryBillingRepository(billingMap)


        commandBus = VertxCommandBus(vertx.eventBus())
        eventBus = VertxEventBus(vertx.eventBus())

        val invoiceService = InvoiceService(invoiceRepository, paymentProvider)
        val paymentService = PaymentService(invoiceRepository, paymentRepository, commandBus)

        billingService = BillingService(billingRepository, invoiceService, commandBus)
        billingSaga = BillingSaga(commandBus)
        paymentSaga = PaymentSaga(commandBus)
        billingCommandHandler = BillingCommandHandler(billingService)
        billingEventHandler = BillingEventHandler(billingSaga)
        paymentCommandHandler = PaymentCommandHandler(paymentService, invoiceService)
        paymentEventHandler = PaymentEventHandler(paymentSaga)
        vertx.deployVerticle(BillingVerticle(
                billingCommandHandler = billingCommandHandler,
                billingEventHandler = billingEventHandler), testContext.completing())

        vertx.deployVerticle(PaymentVerticle(
                paymentCommandHandler = paymentCommandHandler,
                paymentEventHandler = paymentEventHandler
        ), testContext.completing())


    }


    @Test
    fun `should process billing as complete`(vertx: Vertx,
                                             testContext: VertxTestContext) {
        //Given some pending invoices
        val aCustomer = dal.createCustomer(Currency.EUR)

        val invoices = createInvoice(aCustomer!!)

        val invoicesToBeProcessed = invoices.map { it.id }
        every { paymentProvider.charge(any()) } returns true

        //When billing was requested to be created
        billingService.startProcess()
        testContext.awaitCompletion(500, TimeUnit.MILLISECONDS)
        testContext.completeNow()

        //Then process all invoices
        val billingCreated = billingMap.values.first()

        assert(billingCreated != null)
        assert(billingCreated!!.status == BillingStatus.COMPLETED)

        val currentInvoices = billingCreated.invoicesId()
        assert(currentInvoices.containsAll(invoicesToBeProcessed))
        assert(billingCreated.invoices.values.all { it.invoiceStatus == BillingInvoiceStatus.PROCESSED })
        assert(paymentsMap.values.all { it!!.status == PaymentStatus.COMPLETED })
        assert(paymentsMap.values.map { it!!.invoiceId }.containsAll(billingCreated.invoicesId()))

    }

    private fun createInvoice(customer: Customer): List<Invoice> = (1..3).map {
        dal.createInvoice(
                TEN_EURO,
                customer,
                InvoiceStatus.PENDING) ?: throw RuntimeException("fail creating")
    }

}