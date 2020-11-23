package io.pleo.antaeus.context.billing

import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging


class BillingService(private val invoiceService: InvoiceService, private val commandBus: CommandBus) {

    private val logger = KotlinLogging.logger {}

    fun startProcess() = invoiceService.fetchAllPending()
            .map(Invoice::id)
            .takeIf { it.isNotEmpty() }
            ?.let { pendingInvoiceIds -> commandBus.send(StartBillingCommand(pendingInvoiceIds)) }
            ?: logger.info { "Nothing to process" }
}