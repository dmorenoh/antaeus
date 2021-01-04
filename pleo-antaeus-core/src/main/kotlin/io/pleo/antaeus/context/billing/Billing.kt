package io.pleo.antaeus.context.billing

import java.util.*


data class Billing(
    val processId: UUID,
    var status: BillingStatus = BillingStatus.STARTED,
    val invoices: Map<Int, BillingInvoice> = mutableMapOf()
) {

    companion object {
        fun create(command: StartBillingCommand): Billing {
            val processId = UUID.randomUUID()
            return Billing(
                processId = processId,
                status = BillingStatus.STARTED,
                invoices = command.invoicesIds.map { it to BillingInvoice(processId, it) }.toMap()
            )
        }
    }

    fun closeInvoice(invoiceId: Int): Billing {
        invoices[invoiceId]?.close()
        return when {
            allInvoicesClosed() -> copy(status = BillingStatus.COMPLETED)
            else -> this
        }
    }

    private fun allInvoicesClosed() = invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED }

    fun isComplete(): Boolean = this.status == BillingStatus.COMPLETED

    fun invoicesId(): List<Int> = this.invoices.values.map { it.invoiceId }
}