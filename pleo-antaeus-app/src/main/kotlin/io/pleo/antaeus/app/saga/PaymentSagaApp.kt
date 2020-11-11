package io.pleo.antaeus.app.saga

import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.payment.PaymentCreatedEvent
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.CommandBus
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message

class PaymentSagaApp(private val commandBus: CommandBus, private val eventBus: EventBus,
                     private val repository: InvoiceRepository,
                     private val paymentProvider: PaymentProvider,
                     private val vertx: Vertx) {

    suspend fun on(event: PaymentCreatedEvent) {
//
//        val consumer = eventBus.consumer<PayInvoiceCommand>(PayInvoiceCommand::class.simpleName)
//
//        consumer.handler { message ->
//            handler(message)
//        }
//
//        val newPayment = repository.load(event.invoiceId)
//                ?.let { Payment.create(it) }
//
//
//
//        val invoice2 = awaitEvent<Invoice> { newPayment!!.process(paymentProvider) }
//
//        repository.update(invoice2)
//
////        val invoice2 = awaitEvent<Invoice> { handler(PayInvoiceCommand(event.transactionId, event.invoiceId)) }
////        repository.update(invoice2)
//
//
//        val invoiceResult = awaitResult<Message<Invoice>> { h ->
//            eventBus.request<Invoice>(command::class.simpleName, command, h)
//        }
//        if (invoiceResult.)
//            repository.update(invoice = invoiceResult.body())
    }

    private fun handler(message: Message<PayInvoiceCommand>) {
        runCatching {
            val command = message.body()
            repository.load(command.invoiceId)?.pay(provider = paymentProvider)
        }.onFailure {
            message.fail(0, it::class.simpleName)
        }.onSuccess {
            message.reply(it)
        }
    }

}