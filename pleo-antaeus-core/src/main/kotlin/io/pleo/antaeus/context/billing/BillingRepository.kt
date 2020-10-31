package io.pleo.antaeus.context.billing

import java.util.*

interface BillingRepository {
    fun save(billingBatch: Billing):Billing?
    fun update(billingBatch: Billing)
    fun load(processId:UUID): Billing?
}