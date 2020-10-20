package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.models.PaymentFailureReason

data class RequestInvoicePaymentCommand(val transactionId: String, val invoiceId: Int, val processId:Int?) :Command
data class CompleteInvoicePaymentCommand(val transactionId: String, val invoiceId: Int) :Command
data class CancelInvoicePaymentCommand(val transactionId: String, val invoiceId: Int, val paymentFailureReason: PaymentFailureReason) :Command
