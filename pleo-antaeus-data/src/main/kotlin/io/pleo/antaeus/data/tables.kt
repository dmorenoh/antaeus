/*
    Defines database tables and their schemas.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import org.jetbrains.exposed.sql.Table

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
    val version = integer("version")
}

object CustomerTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val currency = varchar("currency", 3)
}

object BillingTransactionTable : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val status = text("status")
}

object InvoicePaymentTransactionTable : Table() {
    val id = uuid("id")
    val invoiceId = reference("invoice_id", InvoiceTable.id)
    val status = text("status")
    val cancellationReason = text("cancellation_reason")
    val billingProcessId = reference("billing_process_id", BillingTransactionTable.id).nullable()
}
