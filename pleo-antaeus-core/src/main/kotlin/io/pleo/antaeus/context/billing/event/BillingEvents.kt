package io.pleo.antaeus.context.billing.event

import io.pleo.antaeus.core.event.Event

data class BillingBatchProcessStartedEvent(val processId: Int) : Event
data class BillingBatchProcessFinishedEvent(val processId: Int) : Event
data class BillingFailedEvent(val invoiceId: Int)
