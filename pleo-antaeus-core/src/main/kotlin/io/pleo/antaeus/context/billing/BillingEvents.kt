package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.event.Event
import java.util.*

data class BillingStartedEvent(val billingId: UUID, val invoices: List<Int>) : Event
data class BillingCompletedEvent(val processId: UUID) : Event
