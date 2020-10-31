package io.pleo.antaeus.data

import io.pleo.antaeus.models.Invoice
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InvoiceDalImpl(private val db: Database) {

    fun update(invoice: Invoice) {
        val rowsUpdated = transaction(db) {
            InvoiceTable
                    .update({ (InvoiceTable.id eq invoice.id) and (InvoiceTable.version eq invoice.version) }) {
                        it[value] = invoice.amount.value
                        it[currency] = invoice.amount.currency.toString()
                        it[status] = invoice.status.name
                        it[customerId] = invoice.customerId
                    }

        }
        if (rowsUpdated == 0)
            throw RuntimeException("Optimistic locking exception")
    }


    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                    .selectAll()
                    .map { it.toInvoice() }
        }
    }

    fun fetchInvoice(id: Int): Invoice? {
        return transaction(db) {
            InvoiceTable
                    .select { InvoiceTable.id.eq(id) }
                    .firstOrNull()
                    ?.toInvoice()
        }
    }
}