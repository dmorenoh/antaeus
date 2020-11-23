package io.pleo.antaeus.context.payment

import io.pleo.antaeus.context.invoice.InvoiceChargedEvent
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.PaymentRevertedEvent
import io.pleo.antaeus.core.messagebus.CommandBus
import io.vertx.core.eventbus.ReplyException
import mu.KotlinLogging

class PaymentSaga(private val commandBus: CommandBus) {

    private val logger = KotlinLogging.logger {}

    suspend fun on(event: PaymentCreatedEvent) = try {
        commandBus.sendAwait(
                command = PayInvoiceCommand(
                        transactionId = event.transactionId,
                        invoiceId = event.invoiceId))
    } catch (e: ReplyException) {
        commandBus.send(
                command = CancelPaymentCommand(
                        transactionId = event.transactionId,
                        invoiceId = event.invoiceId,
                        cancellationDescription = e.message ?: "unknown"))
    }

    suspend fun on(event: InvoicePaidEvent) = try {
        commandBus.sendAwait(
                command = ChargeInvoiceCommand(
                        transactionId = event.transactionId,
                        invoiceId = event.invoiceId))
    } catch (e: ReplyException) {
        commandBus.send(
                command = RevertPaymentCommand(
                        transactionId = event.transactionId,
                        invoiceId = event.invoiceId,
                        reason = e.message ?: "unknown"))
    }

    fun on(event: InvoiceChargedEvent) = commandBus.send(
            command = CompletePaymentCommand(
                    transactionId = event.transactionId,
                    invoiceId = event.invoiceId))

    fun on(event: PaymentRevertedEvent) = commandBus.send(
            command = CancelPaymentCommand(
                    transactionId = event.transactionId,
                    invoiceId = event.invoiceId,
                    cancellationDescription = event.reason))


}