/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getCurrencyExchangeProvider
import getPaymentProvider
import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.customer.CustomerService
import io.pleo.antaeus.context.invoice.InvoiceCommandHandlerOld
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.core.messagebus.VertxCommandBus
import io.pleo.antaeus.core.messagebus.VertxEventBus
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.InvoiceTable

import io.pleo.antaeus.repository.ExposedCustomerRepository
import io.pleo.antaeus.repository.ExposedInvoiceRepository
import io.pleo.antaeus.repository.InMemoryBillingRepository
import io.pleo.antaeus.repository.InMemoryPaymentRepository

import io.pleo.antaeus.rest.AntaeusRest
import io.vertx.core.Vertx
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
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
    val currencyExchangeProvider = getCurrencyExchangeProvider()


    // message bus
    val commandBus = VertxCommandBus(vertx.eventBus())
    val eventBus = VertxEventBus(vertx.eventBus())

    // Create core services

    val invoiceService = InvoiceService(repository = invoiceRepository)
    val customerService = CustomerService(repository = customerRepository)

    val paymentCommandHandler = PaymentCommandHandler(
            repository = paymentRepository,
            eventBus = eventBus)
    val invoiceCommandHandler = InvoiceCommandHandlerOld(
            repository = invoiceRepository,
            paymentProvider = paymentProvider,
            currencyExchangeProvider = currencyExchangeProvider,
            eventBus = eventBus)
    val billingCommandHandler = BillingCommandHandler(
            repository = billingRepository,
            eventBus = eventBus)



    //Billing scheduler
    val scheduler = StdSchedulerFactory.getDefaultScheduler();
    scheduler.start();



    // Create REST web service
    AntaeusRest(
            invoiceService = invoiceService,
            customerService = customerService
    ).run()
}
