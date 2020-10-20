package io.pleo.antaeus.models

data class InvoicePaymentTransaction(val transactionId: String,
                                     val invoiceId: Int,
                                     val status: PaymentStatus = PaymentStatus.STARTED,
                                     val paymentFailureReason: PaymentFailureReason = PaymentFailureReason.NONE,
                                     val billingProcessId: Int?) {

    fun complete(): InvoicePaymentTransaction {
        return copy(status = PaymentStatus.COMPLETED)
    }

    fun canceled(paymentFailureReason: PaymentFailureReason): InvoicePaymentTransaction {
        return copy(paymentFailureReason = paymentFailureReason, status = PaymentStatus.CANCELED)
    }
}