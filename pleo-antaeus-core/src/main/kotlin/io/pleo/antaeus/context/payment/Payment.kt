package io.pleo.antaeus.context.payment

import java.util.*

data class Payment(val transactionId: UUID,
                   val invoiceId: Int,
//                   val invoice: Invoice,
                   val status: PaymentStatus,
                   val paymentCancellationReason: PaymentCancellationReason?,
                   val billingProcessId: UUID? = null) {

    companion object {
        fun create(command: CreatePaymentCommand): Payment {
            return Payment(
                    UUID.randomUUID(),
                    command.invoiceId,
                    PaymentStatus.STARTED,
                    null,
                    command.processId)
        }

//        fun create(invoice: Invoice) = Payment(
//                transactionId = UUID.randomUUID(),
//                invoice = invoice
//        )
    }

//    fun process(paymentProvider: PaymentProvider) {
//
//        runCatching {
//            invoice.pay(provider = paymentProvider)
//        }.onFailure {
//            status.
//            PaymentFailedEvent(transactionId, invoice.id, it)
//        }.onSuccess {
//            PaymentCompletedEvent(transactionId, invoice.id, billingProcessId)
//        }
//
//    }

    fun complete(): Payment {
        if (status != PaymentStatus.STARTED)
            throw InvalidPaymentStatusException("Cannot complete payment '${transactionId}' as is already '${status.name}'")
        return copy(status = PaymentStatus.COMPLETED)
    }

    fun cancel(paymentCancellationReason: PaymentCancellationReason): Payment {
        if (status != PaymentStatus.STARTED)
            throw InvalidPaymentStatusException("Cannot cancel payment '${transactionId}' as is already " +
                    "'${status.name}'")
        return copy(paymentCancellationReason = paymentCancellationReason, status = PaymentStatus.CANCELED)
    }
}