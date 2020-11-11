package io.pleo.antaeus.context.billing

import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

class BillingService(private val invoiceService: InvoiceService, private val commandBus: CommandBus) {
    fun startProcess() {
        val pending = invoiceService.fetchAllPending()
        if (pending.isEmpty()) {
            logger.info { "Nothing to process" }
        } else {
            commandBus.send(StartBillingCommand(UUID.randomUUID(), pending.map { it.id }))
        }
    }
}