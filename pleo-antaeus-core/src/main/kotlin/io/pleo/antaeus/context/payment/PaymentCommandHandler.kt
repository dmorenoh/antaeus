package io.pleo.antaeus.context.payment

import arrow.core.Either
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

class PaymentCommandHandler(private val paymentRepository: PaymentRepository,
                            private val eventBus: EventBus) {

    private val logger = KotlinLogging.logger {}

    suspend fun handleFun(command: CreatePaymentCommand): Either<Throwable, Event> =
            Either.catch {
                Payment.create(command.invoiceId, command.billingId)
                        .also { payment -> paymentRepository.save(payment) }
                        .let { PaymentCreatedEvent(it.transactionId, it.invoiceId, command.billingId) }
                        .also { event -> eventBus.publish(event) }
            }


    fun handle(command: CreatePaymentCommand) {
        logger.info { "Create payment for invoice ${command.invoiceId}" }
        Payment.create(command.invoiceId, command.billingId)
                .also { payment -> paymentRepository.save(payment) }
                .also { eventBus.publish(PaymentCreatedEvent(it.transactionId, it.invoiceId, command.billingId)) }
    }

    fun handle(command: CancelPaymentCommand) {
        logger.info { "Cancel payment for invoice ${command.invoiceId}" }
        paymentRepository.load(command.transactionId)
                ?.cancel(command.cancellationDescription)
                ?.also { payment -> paymentRepository.update(payment) }
                ?.also {
                    logger.info { "sendig PaymentCanceledEvent ${it.invoiceId}" }
                    eventBus.publish(PaymentCanceledEvent(
                            it.transactionId,
                            it.invoiceId,
                            it.cancellationReason,
                            it.billingId))
                }
    }

    fun handle(command: CompletePaymentCommand) {
        logger.info { "Complete payment for invoice ${command.invoiceId}" }

        paymentRepository.load(command.transactionId)
                ?.complete()
                ?.also { payment -> paymentRepository.update(payment) }
                ?.also { eventBus.publish(PaymentCompletedEvent(it.transactionId, it.invoiceId, it.billingId)) }
    }
}