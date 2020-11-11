package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class PaymentCommandHandler(private val repository: PaymentRepository,
                            private val eventBus: EventBus) {


    fun handle(command: CreatePaymentCommand) {

        repository.save(Payment.create(command))
                ?.let {
                    eventBus.publish(PaymentCreatedEvent(
                            it.transactionId,
                            it.invoiceId,
                            it.billingProcessId))
                }
    }

    fun handle(command: CompletePaymentCommand) {

        repository.load(command.transactionId)?.complete()
                ?.let {
                    repository.update(it)
                    eventBus.publish(PaymentCompletedEvent(
                            it.transactionId,
                            it.invoiceId,
                            it.billingProcessId))
                }
    }


    fun handle(command: CancelPaymentCommand) {

        repository.load(command.transactionId)?.cancel(command.paymentCancellationReason)
                ?.let {
                    repository.update(it)
                    eventBus.publish(PaymentCanceledEvent(
                            it.transactionId,
                            it.invoiceId,
                            command.paymentCancellationReason,
                            it.billingProcessId))
                }
    }

}