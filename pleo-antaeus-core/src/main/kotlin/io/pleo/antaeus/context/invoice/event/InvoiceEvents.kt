package io.pleo.antaeus.context.invoice.event

import io.pleo.antaeus.core.event.Event

data class InvoicePaidEvent(var transactionId: String, var invoiceId: Int) : Event
