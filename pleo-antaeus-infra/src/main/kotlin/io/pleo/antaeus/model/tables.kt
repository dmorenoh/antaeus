package io.pleo.antaeus.model

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Table

object InvoiceTable : IntIdTable() {
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status")
    val version = integer("version")
}

object CustomerTable : IntIdTable() {
    val currency = varchar("currency", 3)
}

object BillingsTable : Table() {
    val id = uuid("id")
    val status = text("status")
}

object PaymentTable : Table() {
    val id = uuid("id")
    val invoiceId = reference("invoice_id", InvoiceTable.id)
    val status = text("status")
    val cancellationReason = text("cancellation_reason")
    val billingProcessId = reference("billing_process_id", BillingsTable.id).nullable()
}