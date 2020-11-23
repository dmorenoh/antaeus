/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.billing.BillingSaga
import io.pleo.antaeus.context.billing.BillingService
import io.pleo.antaeus.context.customer.CustomerService
import io.pleo.antaeus.context.invoice.InvoiceCommandHandler
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.payment.PaymentSaga
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.messagebus.VertxCommandBus
import io.pleo.antaeus.messagebus.VertxEventBus
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.InvoiceTable
import io.pleo.antaeus.repository.ExposedCustomerRepository
import io.pleo.antaeus.repository.ExposedInvoiceRepository
import io.pleo.antaeus.repository.InMemoryBillingRepository
import io.pleo.antaeus.repository.InMemoryPaymentRepository
import io.pleo.antaeus.rest.AntaeusRest
import io.pleo.antaeus.verticles.BillingVerticle
import io.pleo.antaeus.verticles.PaymentVerticle
import io.pleo.antaeus.verticles.QuarzVerticle
import io.vertx.core.Vertx
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.quartz.JobKey
import org.quartz.impl.StdSchedulerFactory
import setupInitialData
import java.io.File
import java.sql.Connection

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".2")
    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
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


    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    val vertx = Vertx.vertx()

    // setup repo
    val invoiceRepository = ExposedInvoiceRepository(db)
    val paymentRepository = InMemoryPaymentRepository()
    val billingRepository = InMemoryBillingRepository()
    val customerRepository = ExposedCustomerRepository(db)
    // Get third parties
    val paymentProvider = getPaymentProvider()


    // message bus
    val commandBus = VertxCommandBus(vertx.eventBus())
    val eventBus = VertxEventBus(vertx.eventBus())


    // Create core services
    val invoiceService = InvoiceService(repository = invoiceRepository, paymentProvider = paymentProvider)
    val customerService = CustomerService(repository = customerRepository)
    val billingService = BillingService(invoiceService = invoiceService, commandBus = commandBus)

    // command handlers
    var invoiceCommandHandler = InvoiceCommandHandler(invoiceRepository, invoiceService, eventBus)
    var paymentCommandHandler = PaymentCommandHandler(paymentRepository, eventBus)
    var billingCommandHandler = BillingCommandHandler(billingRepository, eventBus)

    // sagas
    var paymentSaga = PaymentSaga(commandBus)
    var billingSaga = BillingSaga(commandBus)

    //Billing scheduler
    val scheduler = StdSchedulerFactory.getDefaultScheduler();
    scheduler.start();


    // Verticles
    var paymentVerticle = PaymentVerticle(paymentCommandHandler, invoiceCommandHandler, paymentSaga)
    var billlingVerticle = BillingVerticle(billingCommandHandler, billingSaga)
    var quarzVerticle = QuarzVerticle(scheduler, JobKey("Billing Job"), "0 0/1 * 1/1 * ? *", billingService)

    vertx.deployVerticle(paymentVerticle)
    vertx.deployVerticle(billlingVerticle)
    vertx.deployVerticle(quarzVerticle)
    // Create REST web service
    AntaeusRest(
            invoiceService = invoiceService,
            customerService = customerService
    ).run()
}
