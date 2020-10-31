package io.pleo.antaeus.app

import io.pleo.antaeus.context.billing.CompleteBillingCommand
import io.pleo.antaeus.context.billing.StartBillingCommand
import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.payment.CancelPaymentCommand
import io.pleo.antaeus.context.payment.CompletePaymentCommand
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.payment.RequestPaymentCommand
import io.vertx.core.AbstractVerticle

class CommandHandlerVerticle(private val invoiceService: InvoiceService,
                             private val paymentCommandHandler: PaymentCommandHandler,
                             private val billingCommandHandler: BillingCommandHandler) : AbstractVerticle() {

    override fun start() {
        vertx.eventBus().consumer<PayInvoiceCommand>("RequestBillingCommand") {
            invoiceService.on(it.body())
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

    // Optional - called when verticle is undeployed
    override fun stop() {
    }
}