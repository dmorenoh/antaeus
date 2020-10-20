package io.pleo.antaeus.context.invoice.command

import io.pleo.antaeus.core.commands.Command

data class PayInvoiceCommand(val transactionId: String, val invoiceId: Int) : Command
