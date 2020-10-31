package io.pleo.antaeus.context.payment

import java.util.*

data class Payment(val transactionId: UUID,
                   val invoiceId: Int,
                   val status: PaymentStatus = PaymentStatus.STARTED,
                   val paymentCancellationReason: PaymentCancellationReason = PaymentCancellationReason.NONE,
                   val billingProcessId: UUID?) {

    companion object {
        fun create(command: RequestPaymentCommand): Payment {
            return Payment(
                    UUID.randomUUID(),
                    command.invoiceId,
                    PaymentStatus.STARTED,
                    PaymentCancellationReason.NONE,
                    command.processId)
        }
    }

    fun complete(): Payment {
        if (status!=PaymentStatus.STARTED)
            throw InvalidPaymentStatusException("Cannot complete payment '${transactionId}' as is already '${status.name}'")
        return copy(status = PaymentStatus.COMPLETED)
    }

    fun cancel(paymentCancellationReason: PaymentCancellationReason): Payment {
        if (status!=PaymentStatus.STARTED)
            throw InvalidPaymentStatusException("Cannot cancel payment '${transactionId}' as is already " +
                    "'${status.name}'")
        return copy(paymentCancellationReason = paymentCancellationReason, status = PaymentStatus.CANCELED)
    }
}