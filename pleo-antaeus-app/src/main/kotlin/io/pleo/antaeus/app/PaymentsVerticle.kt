package io.pleo.antaeus.app

import io.pleo.antaeus.app.commandhandlers.InvoiceCommandHandler
import io.pleo.antaeus.app.commandhandlers.PaymentCommandHandler
import io.pleo.antaeus.app.saga.PaymentSaga
import io.pleo.antaeus.context.invoice.*
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.core.messagebus.GenericCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class PaymentsVerticle(

        private val paymentCommandHandler: PaymentCommandHandler,
        private val invoiceCommandHandler: InvoiceCommandHandler,
        private val paymentSaga: PaymentSaga) : CoroutineVerticle() {

    override suspend fun start() {
        registerCodecs()

        vertx.eventBus().consumer<CreatePaymentCommand>(CreatePaymentCommand::class.java.simpleName) {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<CompletePaymentCommand>("CompletePaymentCommand") {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<CancelPaymentCommand>("CancelPaymentCommand") {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") { message ->
            launch {
                invoiceCommandHandler.handle(message.body())
            }
        }
        vertx.eventBus().consumer<FixCurrencyInvoiceCommand>("FixCurrencyInvoiceCommand") { message ->
            launch {
                invoiceCommandHandler.handle(message.body())
            }
        }

        vertx.eventBus().consumer<PaymentCreatedEvent>(PaymentCreatedEvent::class.java.simpleName) {
            paymentSaga.on(it.body())
        }

        vertx.eventBus().consumer<InvoicePaidEvent>("InvoicePaidEvent") {
            paymentSaga.on(it.body())
        }

        vertx.eventBus().consumer<PaymentFailedEvent>("PaymentFailedEvent") {
            paymentSaga.on(it.body())
        }

        vertx.eventBus().consumer<InvoiceCurrencyUpdatedEvent>(InvoiceCurrencyUpdatedEvent::class.java.simpleName) {
            paymentSaga.on(it.body())
        }
    }

    fun registerCodecs() {

        vertx.eventBus().registerDefaultCodec(CreatePaymentCommand::class.java,
                GenericCodec(CreatePaymentCommand::class.java))

        vertx.eventBus().registerDefaultCodec(CompletePaymentCommand::class.java,
                GenericCodec(CompletePaymentCommand::class.java))

        vertx.eventBus().registerDefaultCodec(CancelPaymentCommand::class.java,
                GenericCodec(CancelPaymentCommand::class.java))

        vertx.eventBus().registerDefaultCodec(PaymentCreatedEvent::class.java,
                GenericCodec(PaymentCreatedEvent::class.java))

        vertx.eventBus().registerDefaultCodec(PaymentCompletedEvent::class.java,
                GenericCodec(PaymentCompletedEvent::class.java))

        vertx.eventBus().registerDefaultCodec(PaymentCanceledEvent::class.java,
                GenericCodec(PaymentCanceledEvent::class.java))

        vertx.eventBus().registerDefaultCodec(PayInvoiceCommand::class.java,
                GenericCodec(PayInvoiceCommand::class.java))

        vertx.eventBus().registerDefaultCodec(FixCurrencyInvoiceCommand::class.java,
                GenericCodec(FixCurrencyInvoiceCommand::class.java))

        vertx.eventBus().registerDefaultCodec(InvoicePaidEvent::class.java,
                GenericCodec(InvoicePaidEvent::class.java))

        vertx.eventBus().registerDefaultCodec(PaymentFailedEvent::class.java,
                GenericCodec(PaymentFailedEvent::class.java))

        vertx.eventBus().registerDefaultCodec(InvoiceCurrencyUpdatedEvent::class.java,
                GenericCodec(InvoiceCurrencyUpdatedEvent::class.java))
    }
}