package io.pleo.antaeus.context.billing

import mu.KotlinLogging
import java.util.*


data class Billing(val processId: UUID,
                   var status: BillingStatus = BillingStatus.STARTED,
                   val invoices: Map<Int, BillingInvoice> = mutableMapOf()
) {
    private val logger = KotlinLogging.logger {}

    fun closeInvoice(invoiceId: Int): Billing {
        invoices[invoiceId]?.let {
            it.close()
        }
        if (invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED }) {
            logger.info { "Closing billing" }
            this.status = BillingStatus.COMPLETED
        }

        return this
    }

    companion object {

        fun create(command: StartBillingCommand): Billing {
            val processId = UUID.randomUUID()
            return Billing(
                    processId = processId,
                    status = BillingStatus.STARTED,
                    invoices = command.invoicesIds.map { it to BillingInvoice(processId, it) }.toMap())
        }
    }

    fun isComplete(): Boolean = this.status == BillingStatus.COMPLETED

    fun invoicesId(): List<Int> = this.invoices.values.map { it.invoiceId }
}