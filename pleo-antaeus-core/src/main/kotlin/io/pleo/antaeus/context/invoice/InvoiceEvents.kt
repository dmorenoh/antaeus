package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.core.event.Event
import java.util.*

data class InvoicePaidEvent(var transactionId: UUID, var invoiceId: Int) : Event
