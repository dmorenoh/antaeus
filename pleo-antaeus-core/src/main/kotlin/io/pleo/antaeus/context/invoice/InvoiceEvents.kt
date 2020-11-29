package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.core.event.Event
import java.util.*

data class InvoicePaidEvent(var transactionId: UUID,
                            var invoiceId: Int,
                            var invoiceStatus: InvoiceStatus) : Event

data class PaymentRevertedEvent(var transactionId: UUID,
                                var invoiceId: Int,
                                val invoiceStatus: InvoiceStatus,
                                var reason: String) : Event

data class InvoiceChargedEvent(var transactionId: UUID,
                               var invoiceId: Int) : Event
