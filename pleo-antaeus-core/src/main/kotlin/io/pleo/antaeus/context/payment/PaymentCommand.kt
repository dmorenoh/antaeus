package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class RequestPaymentCommand(val invoiceId: Int, val processId:UUID?) :Command
data class CompletePaymentCommand(val transactionId: UUID, val invoiceId: Int) :Command
data class CancelPaymentCommand(val transactionId: UUID, val invoiceId: Int, val paymentCancellationReason: PaymentCancellationReason) :Command
