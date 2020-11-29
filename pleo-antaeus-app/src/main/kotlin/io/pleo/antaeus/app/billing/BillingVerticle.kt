package io.pleo.antaeus.app.billing

import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.verticles.createConsumer
import io.pleo.antaeus.verticles.registerCodec
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class BillingVerticle(private val billingCommandHandler: BillingCommandHandler,
                      private val billingEventHandler: BillingEventHandler) : CoroutineVerticle() {

    private val commandMessage = listOf(
            StartBillingCommand::class,
            CloseBillingInvoiceCommand::class)

    private val eventMessage = listOf(
            BillingStartedEvent::class,
            BillingInvoiceCompletedEvent::class,
            BillingCompletedEvent::class)


    override suspend fun start() {

        val eventBus = vertx.eventBus()

        eventMessage.forEach { eventBus.registerCodec(it.java) }

        registerCommandHandlers(eventBus)
        registerEventsForSaga(eventBus)
    }

    private fun registerEventsForSaga(eventBus: EventBus) {
        listOf(
                BillingStartedEvent::class,
                PaymentCanceledEvent::class,
                PaymentCompletedEvent::class
        )
                .forEach {
                    eventBus
                            .createConsumer(it)
                            .handler { message ->
                                billingEventHandler.handleEvent(message.body())
                            }
                }
    }


    private fun registerCommandHandlers(eventBus: EventBus) {

        commandMessage
                .forEach { commandMessage ->
                    eventBus.registerCodec(commandMessage.java)
                            .createConsumer(commandMessage)
                            .handler { message ->
                                launch {
                                    billingCommandHandler.handle(command = message.body())
                                            .fold(
                                                    ifLeft = { error -> message.fail(1, error::class.simpleName) },
                                                    ifRight = {
                                                        message.reply("done")
                                                        eventBus.send(it::class.simpleName, it)
                                                    }
                                            )
                                }
                            }

                }
    }


}
