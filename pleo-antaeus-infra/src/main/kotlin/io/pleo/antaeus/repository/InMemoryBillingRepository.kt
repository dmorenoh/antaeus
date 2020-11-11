package io.pleo.antaeus.repository

import io.pleo.antaeus.context.billing.Billing
import io.pleo.antaeus.context.billing.BillingRepository
import java.util.*

class InMemoryBillingRepository(private val billingsMap: MutableMap<UUID, Billing?> = mutableMapOf()) : BillingRepository {
    override fun save(billing: Billing): Billing? {
        billingsMap[billing.processId] = billing
        return billing
    }

    override fun update(billing: Billing) {
        billingsMap.replace(billing.processId, billing)
    }

    override fun load(processId: UUID): Billing? {
        return billingsMap[processId]
    }
}