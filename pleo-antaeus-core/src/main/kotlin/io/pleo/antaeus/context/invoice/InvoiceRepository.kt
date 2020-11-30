package io.pleo.antaeus.context.invoice

interface InvoiceRepository {
    fun updateBlocking(invoice: Invoice): Invoice?
    suspend fun update(invoice: Invoice):  Invoice
    fun loadBlocking(id: Int): Invoice?
    suspend fun load(id: Int): Invoice?
    fun fetchByStatus(status: InvoiceStatus): List<Invoice>
    fun fetchAll(): List<Invoice>
}