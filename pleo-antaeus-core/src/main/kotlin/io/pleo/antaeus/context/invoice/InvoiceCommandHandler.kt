package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.EventBus

class InvoiceCommandHandler(private val repository: InvoiceRepository,
                            private val paymentProvider: PaymentProvider,
                            private val eventBus: EventBus) {

    fun on(command: PayInvoiceCommand) {

        val invoice = repository.load(command.invoiceId)
                ?: throw InvoiceNotFoundException(id = command.invoiceId)

        repository.update(invoice.pay(paymentProvider))

        eventBus.publish(
                InvoicePaidEvent(transactionId = command.transactionId,
                        invoiceId = invoice.id))
    }
}