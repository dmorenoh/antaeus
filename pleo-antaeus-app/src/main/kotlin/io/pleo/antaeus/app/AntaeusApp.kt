/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.data.BillingDalImpl
import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.data.InvoiceDalImpl
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.data.InvoicePaymentDalImpl
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.customer.CustomerService
import io.pleo.antaeus.core.messagebus.VertxCommandBus
import io.pleo.antaeus.core.messagebus.VertxEventBus
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import io.vertx.core.Vertx
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import setupInitialData
import java.io.File
import java.sql.Connection

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    val dbFile: File = File.createTempFile("antaeus-db", ".sqlite")
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

    // setup dal
    val invoiceDal = InvoiceDalImpl(dal)
    val billingDal = BillingDalImpl(dal)
    val invoicePaymentDal = InvoicePaymentDalImpl(dal)

    // Get third parties
    val paymentProvider = getPaymentProvider()

    // Vertx is fun
    val vertx = Vertx.vertx()
    val bus = vertx.eventBus()

    // message bus
    val commandBus = VertxCommandBus(vertx.eventBus())
    val eventBus = VertxEventBus(vertx.eventBus())

    // Create core services
    val invoicePaymentService = PaymentCommandHandler(invoicePaymentDal = invoicePaymentDal, eventBus = eventBus)
    val invoiceService = InvoiceService(dal = invoiceDal, paymentProvider = paymentProvider, eventBus = eventBus)
    val billingService = BillingCommandHandler(invoicePaymentService, billingDal, eventBus)
    val customerService = CustomerService(dal = dal)


    // Create REST web service
    AntaeusRest(
            invoiceService = invoiceService,
            customerService = customerService
    ).run()
}
