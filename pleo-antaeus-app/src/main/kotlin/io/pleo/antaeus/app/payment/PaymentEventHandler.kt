package io.pleo.antaeus.app.payment

import io.pleo.antaeus.context.invoice.InvoiceChargedEvent
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.PaymentRevertedEvent
import io.pleo.antaeus.context.payment.PaymentCreatedEvent
import io.pleo.antaeus.context.payment.PaymentSaga
import io.pleo.antaeus.core.event.Event

class PaymentEventHandler(private val paymentSaga: PaymentSaga) {
    suspend fun handle(event: Event) {

        when (event) {
            is PaymentCreatedEvent -> paymentSaga.on(event)
            is InvoicePaidEvent -> paymentSaga.on(event)
            is InvoiceChargedEvent -> paymentSaga.on(event)
            is PaymentRevertedEvent -> paymentSaga.on(event)
        }
    }
}