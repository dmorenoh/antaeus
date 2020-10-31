package io.pleo.antaeus.models

data class BillingProcess(val processId: Int,
                          val status: BillingStatus = BillingStatus.STARTED,
                          val payments: List<InvoicePaymentTransaction>) {
}