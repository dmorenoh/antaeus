package io.pleo.antaeus.context.billing.dal

import io.pleo.antaeus.models.BillingBatchProcess

interface BillingDal {
    fun save(billingBatchProcess: BillingBatchProcess): BillingBatchProcess?
    fun update(billingBatchProcess: BillingBatchProcess)
    fun fetch(processId: Int): BillingBatchProcess
}