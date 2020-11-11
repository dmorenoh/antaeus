package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.messagebus.CommandBus

class InvoiceSaga(private val commandBus: CommandBus) {
    fun on(event: PaymentFailedEvent) {
        if (event.throwable is CurrencyMismatchException)
            commandBus.send(FixCurrencyInvoiceCommand(event.invoiceId))
    }
}