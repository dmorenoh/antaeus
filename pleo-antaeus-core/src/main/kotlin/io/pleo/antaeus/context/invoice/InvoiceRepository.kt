package io.pleo.antaeus.context.invoice

interface InvoiceRepository {
    fun update(invoice: Invoice)
    fun fetch(id: Int): Invoice?
    fun load(id: Int):Invoice?
    fun fetchByStatus(status: InvoiceStatus):List<Invoice>
    fun fetchAll():List<Invoice>
}