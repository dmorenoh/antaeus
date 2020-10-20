package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.models.PaymentFailureReason

data class InvoicePaymentCompletedEvent(var transactionId: String,
                                        var invoiceId: Int,
                                        var billingProcessId: Int?) : Event

data class InvoicePaymentCanceledEvent(var transactionId: String,
                                       var invoiceId: Int,
                                       var paymentFailureReason: PaymentFailureReason,
                                       var billingProcessId: Int?) : Event

data class InvoicePaymentRequestedEvent(var transactionId: String,
                                        var invoiceId: Int,
                                        var billingProcessId: Int? = null) : Event