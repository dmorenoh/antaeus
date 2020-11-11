package io.pleo.antaeus.app.commandhandlers

import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

class BillingCommandHandler(private val repository: BillingRepository,
                            private val eventBus: EventBus) {
    private val logger = KotlinLogging.logger {}

    fun handle(command: StartBillingCommand) {
        repository.save(Billing.create(command))
                ?.let { billing ->
                    eventBus.publish(BillingRequestedEvent(billing.processId, billing.invoices.values.map { it.invoiceId }))
                }
    }

    fun handle(command: CloseBillingInvoiceCommand) {

        repository.load(command.processId)
                ?.let { billing ->
                    billing.closeInvoice(command.invoicesId)
                            .takeIf { it.status == BillingStatus.COMPLETED }
                            ?.apply {
                                eventBus.publish(BillingCompletedEvent(billing.processId))
                            }
                }
    }
}