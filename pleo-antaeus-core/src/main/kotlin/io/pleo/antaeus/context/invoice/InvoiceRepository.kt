package io.pleo.antaeus.context.invoice

interface InvoiceRepository {
    fun update(invoice: Invoice): Invoice?
    suspend fun updateAsync(invoice: Invoice):  Invoice
    fun load(id: Int): Invoice?
    suspend fun loadAsync(id: Int): Invoice?
    fun fetchByStatus(status: InvoiceStatus): List<Invoice>
    fun fetchAll(): List<Invoice>
}