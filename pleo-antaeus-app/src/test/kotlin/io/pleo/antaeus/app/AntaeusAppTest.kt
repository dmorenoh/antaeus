package io.pleo.antaeus.app

import getCurrencyExchangeProvider
import getPaymentProvider
import io.mockk.every
import io.mockk.verify
import io.pleo.antaeus.app.saga.PaymentSaga
import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.customer.CustomerService
import io.pleo.antaeus.context.invoice.InvoiceCommandHandlerOld
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.CreatePaymentCommand
import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.payment.external.CurrencyExchangeProvider
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.messagebus.VertxCommandBus
import io.pleo.antaeus.core.messagebus.VertxEventBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import io.pleo.antaeus.data.AntaeusDal
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

    lateinit var paymentProvider: PaymentProvider
    lateinit var currencyExchangeProvider: CurrencyExchangeProvider
    lateinit var invoiceService: InvoiceService
    lateinit var billingCommandHandler: BillingCommandHandler
    lateinit var paymentCommandHandler: PaymentCommandHandler
    lateinit var invoiceCommandHandlerOld: InvoiceCommandHandlerOld
    lateinit var billingSaga: BillingSaga
    lateinit var paymentSaga: PaymentSaga
    lateinit var commandBus: CommandBus
    lateinit var dal: AntaeusDal
    lateinit var db: Database
    val paymentsMap = mutableMapOf<UUID, Payment?>()
    val billingMap = mutableMapOf<UUID, Billing?>()
    lateinit var billingService: BillingService

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
        val eventBus = VertxEventBus(vertx.eventBus())

        invoiceService = InvoiceService(repository = invoiceRepository)
        val customerService = CustomerService(repository = customerRepository)

        paymentProvider = getPaymentProvider()
        currencyExchangeProvider = getCurrencyExchangeProvider()
        paymentCommandHandler = PaymentCommandHandler(
                repository = paymentRepository,
                eventBus = eventBus)
        invoiceCommandHandlerOld = InvoiceCommandHandlerOld(
                repository = invoiceRepository,
                paymentProvider = paymentProvider,
                currencyExchangeProvider = currencyExchangeProvider,
                eventBus = eventBus)
        billingCommandHandler = BillingCommandHandler(
                repository = billingRepository,
                eventBus = eventBus)

        billingService = BillingService(invoiceService, commandBus)


        billingSaga = BillingSaga(commandBus = commandBus)
        paymentSaga = PaymentSaga(commandBus = commandBus)



//        vertx.deployVerticle(MessageBusVerticle(
//                billingSaga,
//                paymentSagaOld,
//                invoiceCommandHandlerOld,
//                paymentCommandHandler,
//                billingCommandHandler, invoiceRepository, paymentProvider), testContext.completing())

        val scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        vertx.deployVerticle(QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0/1 * 1/1 * ? *", billingService), testContext
                .completing())

    }

    @Test
    fun `should execute job`(vertx: Vertx, testContext: VertxTestContext) {
        //given a bunch of pending invoices
        initData()
        //an scheduler configured
        val scheduler = StdSchedulerFactory.getDefaultScheduler()
        scheduler.start()
        vertx.deployVerticle(QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0/1 * 1/1 * ? *", billingService), testContext
                .completing())

        Thread.sleep(60000)
        val currentBilling = billingMap.values.first()
        assert(currentBilling!!.status == BillingStatus.COMPLETED)
        assert(currentBilling.invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED })
    }

    @Test
    fun `should execute billing`(vertx: Vertx, testContext: VertxTestContext) {
        //given a bunch of pending invoices
        initData()
        assert(dal.fetchPendingInvoice().isNotEmpty())
        // when requesting to execute billing
        billingService.startProcess()
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
        testContext.completeNow()

        // process entire billing
        val currentBilling = billingMap.values.first()
        assert(currentBilling!!.status == BillingStatus.COMPLETED)
        assert(currentBilling.invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED })
    }

    @Test
    fun `should pay invoice when paying pending invoice possible to be processed through payment provider`(vertx: Vertx, testContext: VertxTestContext) {
        //given a pending invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)
        //able to be paid by provider
        every { paymentProvider.charge(invoice!!) } returns true
        //when request to pay invoice
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id, processId = null))
        commandBus.send(CreatePaymentCommand(invoiceId = invoice.id, processId = null))
        commandBus.send(CreatePaymentCommand(invoiceId = invoice.id, processId = null))
        commandBus.send(CreatePaymentCommand(invoiceId = invoice.id, processId = null))
        commandBus.send(CreatePaymentCommand(invoiceId = invoice.id, processId = null))
        commandBus.send(CreatePaymentCommand(invoiceId = invoice.id, processId = null))

        testContext.awaitCompletion(5, TimeUnit.SECONDS)
        testContext.completeNow()
        //invoice paid
        assert(dal.fetchInvoice(invoice.id)!!.status == InvoiceStatus.PAID)
//        assert(dal.fetchPaymentByInvoice(invoice.id)!!.status == PaymentStatus.COMPLETED)
    }

    @Test
    fun `should not pay invoice when paying pending invoice not possible to be processed through payment provider`(vertx: Vertx, testContext: VertxTestContext) {
        //given a pending invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PENDING)
        //able to be paid by provider
        every { paymentProvider.charge(invoice!!) } returns false
        //when request to pay invoice
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id, processId = null))
        testContext.awaitCompletion(2, TimeUnit.SECONDS)
        testContext.completeNow()
        //invoice paid
        assert(dal.fetchInvoice(invoice.id)!! == invoice)
//        val payment = dal.fetchPaymentByInvoice(invoice.id)
//        assert(payment!!.status == PaymentStatus.CANCELED)
//        assert(payment.paymentCancellationReason == PaymentCancellationReason.ACCOUNT_BALANCE_ISSUE)
    }

    @Test
    fun `should not pay invoice when paying paid invoice `(vertx: Vertx, testContext: VertxTestContext) {
        //given a pending invoice
        val aCustomer = dal.createCustomer(Currency.EUR)
        val invoice = dal.createInvoice(
                TEN_EURO,
                aCustomer!!,
                InvoiceStatus.PAID)
        //when request to pay invoice
        commandBus.send(CreatePaymentCommand(invoiceId = invoice!!.id, processId = null))
        testContext.awaitCompletion(2, TimeUnit.SECONDS)
        testContext.completeNow()
        //invoice paid
        assert(dal.fetchInvoice(invoice.id)!! == invoice)
//        val payment = dal.fetchPaymentByInvoice(invoice.id)
//        assert(payment!!.status == PaymentStatus.CANCELED)
//        assert(payment.paymentCancellationReason == PaymentCancellationReason.INVALID_INVOICE_STATUS)
        verify(exactly = 0) { paymentProvider.charge(invoice) }
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