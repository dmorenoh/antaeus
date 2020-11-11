package io.pleo.antaeus.repository

import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.entities.InvoiceEntity
import io.pleo.antaeus.model.InvoiceTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ExposedInvoiceRepository(private val db: Database) : InvoiceRepository {

    override fun update(invoice: Invoice) {
        val rowsUpdated = transaction(db) {
            InvoiceTable
                    .update({ (InvoiceTable.id eq invoice.id) and (InvoiceTable.version eq invoice.version) }) {
                        it[value] = invoice.amount.value
                        it[currency] = invoice.amount.currency.toString()
                        it[status] = invoice.status.name
                    }
        }
        if (rowsUpdated == 0)
            throw RuntimeException("Optimistic locking exception")
    }

    override fun load(id: Int): Invoice? {
        return transaction(db) {
            InvoiceEntity.findById(id)?.toInvoice()
        }
    }

    override fun fetchByStatus(status: InvoiceStatus): List<Invoice> {
        return transaction(db) {
            InvoiceEntity.find { InvoiceTable.status.eq(status.name) }
                    .map { it.toInvoice() }
        }
    }

    override fun fetchAll(): List<Invoice> {
        return transaction(db) {
            InvoiceEntity.all().map { it.toInvoice() }
        }
    }


}