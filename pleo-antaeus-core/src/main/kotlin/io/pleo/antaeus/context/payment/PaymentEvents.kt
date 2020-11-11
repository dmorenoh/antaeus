package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.event.Event
import java.util.*

data class PaymentCreatedEvent(var transactionId: UUID,
                               var invoiceId: Int,
                               var billingProcessId: UUID? = null) : Event

data class PaymentCompletedEvent(var transactionId: UUID,
                                 var invoiceId: Int,
                                 var billingProcessId: UUID?) : Event

data class PaymentCanceledEvent(var transactionId: UUID,
                                var invoiceId: Int,
                                var paymentCancellationReason: PaymentCancellationReason,
                                var billingProcessId: UUID?) : Event


