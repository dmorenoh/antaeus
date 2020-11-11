package io.pleo.antaeus.context.billing

import java.util.*

data class BillingInvoice(val billingId: UUID, val invoiceId: Int, var invoiceStatus: BillingInvoiceStatus =
        BillingInvoiceStatus.STARTED) {
    fun close() {
        this.invoiceStatus = BillingInvoiceStatus.PROCESSED
    }
}