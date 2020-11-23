package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.payment.ChargeInvoiceCommand
import io.pleo.antaeus.context.payment.RevertPaymentCommand
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.EventBus
import mu.KotlinLogging

class InvoiceCommandHandler(private val repository: InvoiceRepository,
                            private val invoiceService: InvoiceService,
                            private val eventBus: EventBus) {
    private val logger = KotlinLogging.logger {}

    suspend fun handle(command: PayInvoiceCommand) {

        logger.info { "About to pay ${command.invoiceId}" }

        val invoice = repository.loadAsync(command.invoiceId) ?: throw InvoiceNotFoundException(command.invoiceId)

        invoice.pay()
                .also { repository.updateAsync(it) }
                .also {
                    logger.info { "sending InvoicePaidEvent ${it.id}" }
                    eventBus.publish(event = InvoicePaidEvent(
                            transactionId = command.transactionId,
                            invoiceId = it.id))
                }
    }

    suspend fun handle(command: ChargeInvoiceCommand) {
        logger.info { "Charging ${command.invoiceId}" }

        invoiceService.charge(command.invoiceId)
        eventBus.publish(event = InvoiceChargedEvent(command.transactionId, command.invoiceId))
    }

    suspend fun handle(command: RevertPaymentCommand) {

        logger.info { "Reverting ${command.invoiceId}" }

        repository.loadAsync(command.invoiceId)
                ?.revertPayment()
                ?.also { invoice -> repository.updateAsync(invoice) }
                ?.also {
                    logger.info { "Sending PaymentRevertedEvent " }
                    eventBus.publish(event = PaymentRevertedEvent(
                            transactionId = command.transactionId,
                            invoiceId = it.id,
                            reason = command.reason))
                }
    }
}