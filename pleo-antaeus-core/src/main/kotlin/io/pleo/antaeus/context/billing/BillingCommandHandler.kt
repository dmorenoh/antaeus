package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.exceptions.BillingProcessNotFoundException

import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

class BillingCommandHandler(private val repository: BillingRepository,
                            private val eventBus: EventBus) {

    private val logger = KotlinLogging.logger {}

    fun handle(command: StartBillingCommand) {
        val billing = repository.save(Billing.create(command.processId))
                ?: throw InvalidBillingTransactionException("no possible to be created")

        eventBus.publish(BillingRequestedEvent(billing.processId))
    }

    fun handle(command: CompleteBillingCommand) {

        val billingProcess = repository.load(command.processId)
                ?: throw BillingProcessNotFoundException(command.processId.toString())

        try {
            repository.update(billingProcess.complete())
            eventBus.publish(BillingCompletedEvent(billingProcess.processId))
        } catch (e: BillingPendingPaymentsException) {
            logger.info { "Billing has still pending payments to be processed" }
        }
    }
}
