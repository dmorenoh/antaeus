package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.event.Event
import java.util.*

data class BillingRequestedEvent(val processId: UUID) : Event
data class BillingCompletedEvent(val processId: UUID) : Event
