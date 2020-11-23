package io.pleo.antaeus.context.billing

import arrow.core.Either
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging


class BillingCommandHandler(private val repository: BillingRepository,
                            private val eventBus: EventBus) {

    private val logger = KotlinLogging.logger {}

    suspend fun handleFun(command: StartBillingCommand): Either<Throwable, Event> = Either.catch {
        Billing.create(command = command)
                .also { repository.save(billing = it) }
                .let { BillingStartedEvent(it.processId, it.invoicesId()) }
                .also { eventBus.publish(it) }
    }


    suspend fun handleFun(command: CloseBillingInvoiceCommand): Either<Throwable, Event> = Either.catch {
        repository.load(command.billingId)
                ?.closeInvoice(command.invoicesId)
                ?.let { BillingCompletedEvent(it.processId) }
                ?.also { eventBus.publish(it) } ?: throw RuntimeException("fail")
    }

    fun handle(command: StartBillingCommand) = Billing.create(command = command)
            .also { repository.save(billing = it) }
            .also { eventBus.publish(event = BillingStartedEvent(it.processId, it.invoicesId())) }


    fun handle(command: CloseBillingInvoiceCommand) = repository.load(command.billingId)
            ?.let { billing -> billing.closeInvoice(command.invoicesId) }
            ?.takeIf { it.isComplete() }
            ?.also { billing -> eventBus.publish(BillingCompletedEvent(billing.processId)) }


}
