package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.app.billing.BillingCommandHandler
import io.pleo.antaeus.app.billing.BillingEventHandler
import io.pleo.antaeus.app.billing.BillingVerticle
import io.pleo.antaeus.app.payment.PaymentCommandHandler
import io.pleo.antaeus.app.payment.PaymentEventHandler
import io.pleo.antaeus.app.verticle.PaymentVerticle
import io.pleo.antaeus.context.billing.*
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
import io.pleo.antaeus.repository.ExposedCustomerRepository
import io.pleo.antaeus.repository.ExposedInvoiceRepository
import io.pleo.antaeus.repository.InMemoryBillingRepository
import io.pleo.antaeus.repository.InMemoryPaymentRepository
import io.pleo.antaeus.verticles.QuarzVerticle
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
import org.quartz.JobKey
import org.quartz.impl.StdSchedulerFactory
import java.io.File
import java.math.BigDecimal
import java.sql.Connection
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random


@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AntaeusAppTest {

    companion object {
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private lateinit var billingCommandHandler: BillingCommandHandler
    private lateinit var billingEventHandler: BillingEventHandler
    private lateinit var paymentCommandHandler: PaymentCommandHandler
    lateinit var paymentEventHandler: PaymentEventHandler
    lateinit var paymentProvider: PaymentProvider
    lateinit var invoiceService: InvoiceService


    lateinit var commandBus: CommandBus
    lateinit var eventBus: EventBus
    lateinit var dal: AntaeusDal
    lateinit var db: Database
    val paymentsMap = mutableMapOf<UUID, Payment?>()
    val billingMap = mutableMapOf<UUID, Billing?>()
    lateinit var billingService: BillingService

    private lateinit var billingSaga: BillingSaga
    private lateinit var paymentSaga: PaymentSaga

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
        val customerRepository = ExposedCustomerRepository(db)

        val paymentRepository = InMemoryPaymentRepository(paymentsMap)
        val billingRepository = InMemoryBillingRepository(billingMap)

        commandBus = VertxCommandBus(vertx.eventBus())
        eventBus = VertxEventBus(vertx.eventBus())
        paymentProvider = getPaymentProvider()
        invoiceService = InvoiceService(repository = invoiceRepository, paymentProvider = paymentProvider)
        val paymentService = PaymentService(invoiceRepository, paymentRepository, commandBus)

        billingSaga = BillingSaga(commandBus)
        paymentSaga = PaymentSaga(commandBus)


        billingService = BillingService(billingRepository, invoiceService, commandBus)
        billingCommandHandler = BillingCommandHandler(billingService)
        billingEventHandler = BillingEventHandler(billingSaga)
        paymentCommandHandler = PaymentCommandHandler(paymentService, invoiceService)
        paymentEventHandler = PaymentEventHandler(paymentSaga)






        vertx.deployVerticle(BillingVerticle(
                commandHandler = billingCommandHandler,
                eventHandler = billingEventHandler), testContext.completing())

        vertx.deployVerticle(PaymentVerticle(
                commandHandler = paymentCommandHandler,
                eventHandler = paymentEventHandler
        ), testContext.completing())

        val scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        vertx.deployVerticle(QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0/1 * 1/1 * ? *", billingService), testContext
                .completing())

    }

    @Test
    fun `should execute job`(testContext: VertxTestContext) {
        //given a bunch of pending invoices
        initData()
        testContext.awaitCompletion(500, TimeUnit.MILLISECONDS)
        testContext.completeNow()
        Thread.sleep(60000)
        val currentBilling = billingMap.values.first()
        assert(currentBilling!!.status == BillingStatus.COMPLETED)
        assert(currentBilling.invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED })
        assert(paymentsMap.values.none { it!!.status == PaymentStatus.STARTED })
        val paymentsCancelled = paymentsMap.values.filter { it!!.status == PaymentStatus.CANCELED }
        val paymentsComplete = paymentsMap.values.filter { it!!.status == PaymentStatus.COMPLETED }
        val allInvoices = dal.fetchAllInvoices()
        val pendingInvoices = allInvoices.filter { it.status == InvoiceStatus.PENDING }
        val paidInvoices = allInvoices.filter { it.status == InvoiceStatus.PAID }
        assert(pendingInvoices.map { it.id }.containsAll(paymentsCancelled.map { it!!.invoiceId }))
        assert(paidInvoices.map { it.id }.containsAll(paymentsComplete.map { it!!.invoiceId }))

    }


    private fun initData() {
        val customers = (1..100).mapNotNull {
            dal.createCustomer(
                    currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
            )
        }

        customers.forEach { customer ->
            (1..10).forEach {
                dal.createInvoice(
                        amount = Money(
                                value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                                currency = customer.currency
                        ),
                        customer = customer,
                        status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
                )
            }
        }
    }
}