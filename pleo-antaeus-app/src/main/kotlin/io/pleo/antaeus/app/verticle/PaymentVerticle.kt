package io.pleo.antaeus.app.verticle

import io.pleo.antaeus.app.AppVerticle
import io.pleo.antaeus.app.payment.PaymentCommandHandler
import io.pleo.antaeus.app.payment.PaymentEventHandler
import io.pleo.antaeus.context.invoice.InvoiceChargedEvent
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.PaymentRevertedEvent
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.verticles.registerCodec

class PaymentVerticle(
        private val commandHandler: PaymentCommandHandler,
        private val eventHandler: PaymentEventHandler
) : AppVerticle() {

    override suspend fun start() {

        val eventBus = vertx.eventBus()

        registerCommands( listOf(
                CreatePaymentCommand::class,
                PayInvoiceCommand::class,
                ChargeInvoiceCommand::class,
                CompletePaymentCommand::class,
                RevertPaymentCommand::class,
                CancelPaymentCommand::class), commandHandler::handle)

        listOf(
                PaymentCreatedEvent::class,
                InvoicePaidEvent::class,
                InvoiceChargedEvent::class,
                PaymentCompletedEvent::class,
                PaymentRevertedEvent::class,
                PaymentCanceledEvent::class).forEach { eventBus.registerCodec(it.java) }

        registerEventHandlers(listOf(
                PaymentCreatedEvent::class,
                InvoicePaidEvent::class,
                InvoiceChargedEvent::class,
                PaymentRevertedEvent::class
        ), eventHandler::handle)

    }
}