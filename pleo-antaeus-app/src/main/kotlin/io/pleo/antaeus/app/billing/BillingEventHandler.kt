package io.pleo.antaeus.app.billing

import io.pleo.antaeus.context.billing.BillingSaga
import io.pleo.antaeus.context.billing.BillingStartedEvent
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.core.event.Event

class BillingEventHandler(private val billingSaga: BillingSaga) {

    fun handleEvent(event: Event) {
        when (event) {
            is BillingStartedEvent -> billingSaga.on(event)
            is PaymentCompletedEvent -> billingSaga.on(event)
            is PaymentCanceledEvent -> billingSaga.on(event)
        }
    }
}