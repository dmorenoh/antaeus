package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class PayInvoiceCommand(val transactionId: UUID, val invoiceId: Int) : Command
