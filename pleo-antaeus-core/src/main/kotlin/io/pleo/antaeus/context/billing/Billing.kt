package io.pleo.antaeus.context.billing

import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}


data class Billing(val processId: UUID,
                   var status: BillingStatus = BillingStatus.STARTED,
                   val invoices: Map<Int, BillingInvoice> = mutableMapOf()
) {
//    fun complete() {
//        logger.info { "Payments size ${invoices.size}" }
//        if (status == BillingStatus.COMPLETED)
//            throw BillingStatusException(processId.toString())
//        if (invoices.any { it.invoiceStatus == BillingInvoiceStatus.STARTED })
//            throw BillingPendingPaymentsException(processId.toString())
//        status = BillingStatus.COMPLETED
//    }

    fun closeInvoice(invoiceId: Int): Billing {
        invoices[invoiceId]?.let {
            it.invoiceStatus = BillingInvoiceStatus.PROCESSED
        }
        if (invoices.values.none { it.invoiceStatus == BillingInvoiceStatus.STARTED }){
            logger.info { "Closing billing" }
            this.status = BillingStatus.COMPLETED
        }

        return this
    }

    companion object {

        fun create(command: StartBillingCommand): Billing {
            return Billing(
                    processId = command.processId,
                    status = BillingStatus.STARTED,
                    invoices = command.invoicesIds.map { it to BillingInvoice(command.processId, it) }.toMap())
        }
    }
}