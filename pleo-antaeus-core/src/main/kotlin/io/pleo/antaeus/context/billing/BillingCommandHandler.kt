package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
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
        logger.info { "Closing billing invoice for invoice:${command.invoicesId} and billing ${command.processId}" }
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
