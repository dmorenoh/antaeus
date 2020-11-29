package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.event.Event
import java.util.*

data class PaymentCreatedEvent(var transactionId: UUID,
                               var invoiceId: Int,
                               val billingId: UUID? = null,
                               var status: PaymentStatus) : Event

data class PaymentCompletedEvent(var transactionId: UUID,
                                 val invoiceId: Int,
                                 var billingId: UUID?,
                                 var status: PaymentStatus) : Event

data class PaymentCanceledEvent(var transactionId: UUID,
                                var invoiceId: Int,
                                var paymentCancellationReason: String?,
                                var billingId: UUID?,
                                var status: PaymentStatus) : Event


