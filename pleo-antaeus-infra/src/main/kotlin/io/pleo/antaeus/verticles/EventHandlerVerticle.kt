package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.billing.BillingRequestedEvent
import io.pleo.antaeus.context.billing.BillingSaga
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.context.payment.PaymentRequestedEvent
import io.pleo.antaeus.context.payment.PaymentSaga
import io.vertx.core.AbstractVerticle

class EventHandlerVerticle(private val billingSaga: BillingSaga,
                           private val paymentSaga: PaymentSaga) : AbstractVerticle() {
    override fun start() {
        vertx.eventBus().consumer<BillingRequestedEvent>("BillingRequestedEvent"){
            billingSaga.on(it.body())
        }
        vertx.eventBus().consumer<PaymentCanceledEvent>("PaymentCanceledEvent"){
            billingSaga.on(it.body())
        }
        vertx.eventBus().consumer<PaymentCompletedEvent>("PaymentCompletedEvent"){
            billingSaga.on(it.body())
        }

        vertx.eventBus().consumer<PaymentRequestedEvent>("PaymentRequestedEvent"){
            paymentSaga.on(it.body())
        }
        vertx.eventBus().consumer<InvoicePaidEvent>("InvoicePaidEvent"){
            paymentSaga.on(it.body())
        }
    }
}