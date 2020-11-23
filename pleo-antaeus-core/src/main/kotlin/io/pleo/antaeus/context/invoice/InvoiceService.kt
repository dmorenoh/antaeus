/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import mu.KotlinLogging

class InvoiceService(private val repository: InvoiceRepository,
                     private val paymentProvider: PaymentProvider) {
    private val logger = KotlinLogging.logger {}

    fun fetchAllPending(): List<Invoice> {
        return repository.fetchByStatus(InvoiceStatus.PENDING)
    }

    fun fetchAll(): List<Invoice> {
        return repository.fetchAll()
    }

    fun fetch(id: Int): Invoice {
        return repository.load(id) ?: throw InvoiceNotFoundException(id)
    }

    suspend fun charge(invoiceId: Int) {
        repository.loadAsync(invoiceId)
                ?.let { invoice -> paymentProvider.charge(invoice) }
                .takeIf { it == false }
                ?.apply { throw AccountBalanceException("No money") }
                ?.run { logger.info { "Charged!" } }

    }
}
