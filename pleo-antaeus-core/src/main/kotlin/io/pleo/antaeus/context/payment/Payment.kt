package io.pleo.antaeus.context.payment

import java.util.*

data class Payment(val transactionId: UUID,
                   val invoiceId: Int,
                   var status: PaymentStatus = PaymentStatus.STARTED,
                   var cancellationReason: String? = "N/A",
                   val billingId: UUID? = null) {

    companion object {
        fun create(command: CreatePaymentCommand): Payment = Payment(
                transactionId = UUID.randomUUID(),
                invoiceId = command.invoiceId,
                billingId = command.billingId
        )

    }

    fun cancel(throwable: String): Payment = copy(status = PaymentStatus.CANCELED, cancellationReason = throwable)

    fun complete(): Payment = copy(status = PaymentStatus.COMPLETED)

}