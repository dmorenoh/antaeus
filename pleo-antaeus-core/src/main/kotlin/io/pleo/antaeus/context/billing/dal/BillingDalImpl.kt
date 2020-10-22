package io.pleo.antaeus.context.billing.dal

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.BillingBatchProcess

class BillingDalImpl(private val antaeusDal: AntaeusDal) : BillingDal {

    override fun save(billingBatchProcess: BillingBatchProcess): BillingBatchProcess? = antaeusDal.createBillingProcess()

    override fun update(billingBatchProcess: BillingBatchProcess) = antaeusDal.updateBillingProcess(billingBatchProcess)

    override fun fetch(processId: Int): BillingBatchProcess? = antaeusDal.fetchBillingTransaction(processId)

}