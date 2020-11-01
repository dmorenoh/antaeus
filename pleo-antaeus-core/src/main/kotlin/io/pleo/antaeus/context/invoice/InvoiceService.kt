/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException

class InvoiceService(private val repository: InvoiceRepository) {

    fun fetchAllPending(): List<Invoice> {
        return repository.fetchByStatus(InvoiceStatus.PENDING)
    }

    fun fetchAll(): List<Invoice> {
        return repository.fetchAll()
    }

    fun fetch(id: Int): Invoice {
        return repository.load(id) ?: throw InvoiceNotFoundException(id)
    }
}
