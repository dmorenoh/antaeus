package io.pleo.antaeus.app.billing

import io.pleo.antaeus.app.AppVerticle
import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.verticles.registerCodec

class BillingVerticle(private val commandHandler: BillingCommandHandler,
                      private val eventHandler: BillingEventHandler) : AppVerticle() {

    override suspend fun start() {

        val eventBus = vertx.eventBus()

        registerCommands(listOf(
                StartBillingCommand::class,
                CloseBillingInvoiceCommand::class), commandHandler::handle)

        listOf(
                BillingStartedEvent::class,
                BillingInvoiceCompletedEvent::class,
                BillingCompletedEvent::class).forEach { eventBus.registerCodec(it.java) }

        registerEventHandlers(listOf(
                BillingStartedEvent::class,
                PaymentCanceledEvent::class,
                PaymentCompletedEvent::class
        ), eventHandler::handle)

    }
}
