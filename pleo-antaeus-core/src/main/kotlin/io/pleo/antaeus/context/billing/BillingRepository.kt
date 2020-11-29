package io.pleo.antaeus.context.billing

import java.util.*

interface BillingRepository {
    fun save(billing: Billing): Billing?
    fun update(billing: Billing)
    fun load(processId: UUID): Billing?
    fun fetchAll(): List<Billing>
}