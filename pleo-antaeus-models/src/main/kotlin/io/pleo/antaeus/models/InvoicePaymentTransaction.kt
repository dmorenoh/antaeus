package io.pleo.antaeus.models

import java.util.*

data class InvoicePaymentTransaction(val transactionId: UUID,
                                     val invoiceId: Int,
                                     val status: PaymentStatus = PaymentStatus.STARTED,
                                     val cancellationReason: CancellationReason = CancellationReason.NONE,
                                     val billingProcessId: Int?) {

    fun complete(): InvoicePaymentTransaction {
        return copy(status = PaymentStatus.COMPLETED)
    }

    fun canceled(cancellationReason: CancellationReason): InvoicePaymentTransaction {
        return copy(cancellationReason = cancellationReason, status = PaymentStatus.CANCELED)
    }
}