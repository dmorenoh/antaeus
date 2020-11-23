package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.invoice.*
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.core.event.Event
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class PaymentVerticle(
        private val paymentCommandHandler: PaymentCommandHandler,
        private val invoiceCommandHandler: InvoiceCommandHandler,
        private val paymentSaga: PaymentSaga
) : CoroutineVerticle() {

    private val commands = listOf(
            CreatePaymentCommand::class.java,
            PayInvoiceCommand::class.java,
            ChargeInvoiceCommand::class.java,
            CompletePaymentCommand::class.java,
            RevertPaymentCommand::class.java,
            CancelPaymentCommand::class.java)

    private val events = listOf(
            PaymentCreatedEvent::class.java,
            InvoicePaidEvent::class.java,
            InvoiceChargedEvent::class.java,
            PaymentCompletedEvent::class.java,
            PaymentRevertedEvent::class.java,
            PaymentCanceledEvent::class.java)

    override suspend fun start() {

        val eventBus = vertx.eventBus()

        commands.forEach { eventBus.registerCodec(it) }

        eventBus.consumerOf(CreatePaymentCommand::class)
                .handler {
                    commandHandler(it, paymentCommandHandler::handle)
                }

        eventBus.consumerOf(PayInvoiceCommand::class)
                .handler {
                    launch { commandHandlerSuspend(it, invoiceCommandHandler::handle) }
                }

        eventBus.consumerOf(ChargeInvoiceCommand::class)
                .handler {
                    launch { commandHandlerSuspend(it, invoiceCommandHandler::handle) }
                }

        eventBus.consumerOf(CompletePaymentCommand::class)
                .handler { commandHandler(it, paymentCommandHandler::handle) }

        eventBus.consumerOf(RevertPaymentCommand::class)
                .handler {
                    launch { commandHandlerSuspend(it, invoiceCommandHandler::handle) }
                }

        eventBus.consumerOf(CancelPaymentCommand::class)
                .handler { commandHandler(it, paymentCommandHandler::handle) }


        events.forEach { eventBus.registerCodec(it) }

        eventBus.consumerOf(PaymentCreatedEvent::class)
                .handler {
                    launch { eventHandlerSuspend(it, paymentSaga::on) }
                }

        eventBus.consumerOf(InvoicePaidEvent::class)
                .handler {
                    launch { eventHandlerSuspend(it, paymentSaga::on) }
                }

        eventBus.consumerOf(InvoiceChargedEvent::class)
                .handler {
                    eventHandler(it, paymentSaga::on)
                }
        eventBus.consumerOf(PaymentRevertedEvent::class)
                .handler {
                    eventHandler(it, paymentSaga::on)
                }

    }

    private suspend fun <T : Event> eventHandlerSuspend(message: Message<T>, hand: suspend (command: T) -> Unit) {
        try {
            hand(message.body())
            message.reply("done")
        } catch (e: Throwable) {
            message.fail(1, e::class.simpleName)
        }
    }

    private fun <T : Event> eventHandler(message: Message<T>, hand: (command: T) -> Unit) {
        try {
            hand(message.body())
            message.reply("done")
        } catch (e: Throwable) {
            message.fail(1, e::class.simpleName)
        }
    }

    private suspend fun <T : Command> commandHandlerSuspend(message: Message<T>, hand: suspend (command: T) -> Unit) {
        try {
            hand(message.body())
            message.reply("done")
        } catch (e: Throwable) {
            message.fail(1, e::class.simpleName)
        }
    }


    private fun <T : Command> commandHandler(message: Message<T>, hand: (command: T) -> Unit) {
        try {
            hand(message.body())
            message.reply("done")
        } catch (e: Throwable) {
            message.fail(1, e::class.simpleName)
        }
    }
}