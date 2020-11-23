package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class CreatePaymentCommand(val invoiceId: Int, val billingId: UUID? = null) : Command

data class CompletePaymentCommand(val transactionId: UUID, val invoiceId: Int) : Command

data class CancelPaymentCommand(val transactionId: UUID,
                                val invoiceId: Int,
                                val cancellationDescription: String) : Command

data class ChargeInvoiceCommand(val transactionId: UUID, val invoiceId: Int) : Command

data class RevertPaymentCommand(val transactionId: UUID, val invoiceId: Int, val reason: String) : Command
