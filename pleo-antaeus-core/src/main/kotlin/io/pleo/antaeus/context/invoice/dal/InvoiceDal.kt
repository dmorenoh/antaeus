package io.pleo.antaeus.context.invoice.dal

import io.pleo.antaeus.models.Invoice

interface InvoiceDal {
    fun update(invoice: Invoice)
    fun fetchInvoices(): List<Invoice>
    fun fetchInvoice(id: Int): Invoice
}