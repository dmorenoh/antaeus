package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.payment.PaymentCommand
import java.util.*

data class PayInvoiceCommand(val transactionId: UUID, val invoiceId: Int) : PaymentCommand
