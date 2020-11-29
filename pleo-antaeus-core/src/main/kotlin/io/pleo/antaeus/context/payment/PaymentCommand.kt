package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.commands.Command
import java.util.*

interface PaymentCommand : Command

data class CreatePaymentCommand(val invoiceId: Int, val billingId: UUID? = null) : PaymentCommand

data class CompletePaymentCommand(val transactionId: UUID, val invoiceId: Int) : PaymentCommand

data class CancelPaymentCommand(val transactionId: UUID,
                                val invoiceId: Int,
                                val cancellationDescription: String) : PaymentCommand

data class ChargeInvoiceCommand(val transactionId: UUID, val invoiceId: Int) : PaymentCommand

data class RevertPaymentCommand(val transactionId: UUID, val invoiceId: Int, val reason: String) : PaymentCommand
