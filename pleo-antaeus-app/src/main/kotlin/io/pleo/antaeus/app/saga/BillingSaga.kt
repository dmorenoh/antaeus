package io.pleo.antaeus.app.saga

import io.pleo.antaeus.context.billing.BillingRequestedEvent
import io.pleo.antaeus.context.billing.CloseBillingInvoiceCommand
import io.pleo.antaeus.context.payment.CreatePaymentCommand
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.core.messagebus.CommandBus

class BillingSaga(private val commandBus: CommandBus) {

    fun on(event: BillingRequestedEvent) {
        event.invoices
                .forEach {
                    commandBus.send(
                            command = CreatePaymentCommand(
                                    invoiceId = it,
                                    processId = event.processId)
                    )
                }
    }

    fun on(event: PaymentCanceledEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CloseBillingInvoiceCommand(it, event.invoiceId)) }
    }

    fun on(event: PaymentCompletedEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CloseBillingInvoiceCommand(it, event.invoiceId)) }
    }
}