package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.billing.CompleteBillingCommand
import io.pleo.antaeus.context.billing.StartBillingCommand
import io.pleo.antaeus.context.invoice.InvoiceCommandHandler
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.payment.CancelPaymentCommand
import io.pleo.antaeus.context.payment.CompletePaymentCommand
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.payment.RequestPaymentCommand
import io.vertx.core.AbstractVerticle

class CommandHandlerVerticle(private val invoiceCommandHandler: InvoiceCommandHandler,
                             private val paymentCommandHandler: PaymentCommandHandler,
                             private val billingCommandHandler: BillingCommandHandler) : AbstractVerticle() {

    override fun start() {
        vertx.eventBus().consumer<PayInvoiceCommand>("RequestBillingCommand") {
            invoiceCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<RequestPaymentCommand>("RequestInvoicePaymentCommand") {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<CompletePaymentCommand>("CompleteInvoicePaymentCommand") {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<CancelPaymentCommand>("CancelInvoicePaymentCommand") {
            paymentCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<StartBillingCommand>("StartBillingBatchProcessCommand") {
            billingCommandHandler.handle(it.body())
        }

        vertx.eventBus().consumer<CompleteBillingCommand>("CompleteBillingBatchProcessCommand") {
            billingCommandHandler.handle(it.body())
        }
    }

}