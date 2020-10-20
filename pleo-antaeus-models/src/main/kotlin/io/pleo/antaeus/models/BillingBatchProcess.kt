package io.pleo.antaeus.models

data class BillingBatchProcess(val processId: Int, val status: BillingStatus = BillingStatus.STARTED) {
    fun complete(): BillingBatchProcess {
        return copy(status = BillingStatus.COMPLETED)
    }
}