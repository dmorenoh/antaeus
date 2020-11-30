package io.pleo.antaeus.repository

import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.entities.InvoiceEntity
import io.pleo.antaeus.model.InvoiceTable
import io.vertx.kotlin.coroutines.awaitBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ExposedInvoiceRepository(private val db: Database) : InvoiceRepository {

    private val logger = KotlinLogging.logger {}

    override fun updateBlocking(invoice: Invoice): Invoice {
        val rowsUpdated = transaction(db) {
            InvoiceTable
                    .update({ (InvoiceTable.id eq invoice.id) and (InvoiceTable.version eq invoice.version) }) {
                        it[value] = invoice.amount.value
                        it[currency] = invoice.amount.currency.toString()
                        it[status] = invoice.status.name
                        it[version] = invoice.version + 1
                    }
        }
        if (rowsUpdated == 0) {
            logger.info { "Failed to update ${invoice.id} to status ${invoice.status}" }
            throw RuntimeException("Not found or optimistic locking exception when updating ${invoice.id}")
        }
        return loadBlocking(invoice.id) ?: throw InvoiceNotFoundException(invoice.id)
    }

    override suspend fun update(invoice: Invoice): Invoice = awaitBlocking { updateBlocking(invoice) }

    override fun loadBlocking(id: Int): Invoice? = transaction(db) {
        InvoiceEntity.findById(id)?.toInvoice()
    }


    override suspend fun load(id: Int): Invoice? = awaitBlocking { loadBlocking(id) }


    override fun fetchByStatus(status: InvoiceStatus): List<Invoice> = transaction(db) {
        InvoiceEntity.find { InvoiceTable.status.eq(status.name) }
                .map { it.toInvoice() }
    }


    override fun fetchAll(): List<Invoice> = transaction(db) {
        InvoiceEntity.all().map { it.toInvoice() }
    }


}