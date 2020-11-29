package io.pleo.antaeus.app.verticle

import io.pleo.antaeus.app.payment.PaymentCommandHandler
import io.pleo.antaeus.app.payment.PaymentEventHandler
import io.pleo.antaeus.context.invoice.InvoiceChargedEvent
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.PaymentRevertedEvent
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.verticles.createConsumer
import io.pleo.antaeus.verticles.registerCodec
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class PaymentVerticle(
        private val paymentCommandHandler: PaymentCommandHandler,
        private val paymentEventHandler: PaymentEventHandler
) : CoroutineVerticle() {

    private val commandMessage = listOf(
            CreatePaymentCommand::class,
            PayInvoiceCommand::class,
            ChargeInvoiceCommand::class,
            CompletePaymentCommand::class,
            RevertPaymentCommand::class,
            CancelPaymentCommand::class)

    private val eventMessage = listOf(
            PaymentCreatedEvent::class,
            InvoicePaidEvent::class,
            InvoiceChargedEvent::class,
            PaymentCompletedEvent::class,
            PaymentRevertedEvent::class,
            PaymentCanceledEvent::class)


    override suspend fun start() {

        val eventBus = vertx.eventBus()

        registerCommandHandlers(eventBus)
        eventMessage.forEach { eventBus.registerCodec(it.java) }
        registerEventsForSaga(eventBus)


    }

    private fun registerCommandHandlers(eventBus: EventBus) {

        commandMessage
                .forEach { commandMessage ->
                    eventBus.registerCodec(commandMessage.java)
                            .createConsumer(commandMessage)
                            .handler { message ->
                                launch {
                                    paymentCommandHandler.handle(command = message.body())
                                            .fold(
                                                    ifLeft = { error ->
                                                        message.fail(1, error::class.simpleName)
                                                    },
                                                    ifRight = {
                                                        message.reply("done")
                                                        eventBus.send(it::class.simpleName, it)
                                                    }
                                            )
                                }
                            }

                }
    }

    private fun registerEventsForSaga(eventBus: EventBus) {
        listOf(
                PaymentCreatedEvent::class,
                InvoicePaidEvent::class,
                InvoiceChargedEvent::class,
                PaymentRevertedEvent::class
        )
                .forEach {
                    eventBus
                            .createConsumer(it)
                            .handler { message ->
                                launch { paymentEventHandler.handleEvent(message.body()) }
                            }
                }
    }


}