package io.pleo.antaeus.app

import io.pleo.antaeus.context.billing.command.CompleteBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.command.StartBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.service.BillingService
import io.pleo.antaeus.context.invoice.command.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.context.payment.CancelInvoicePaymentCommand
import io.pleo.antaeus.context.payment.CompleteInvoicePaymentCommand
import io.pleo.antaeus.context.payment.InvoicePaymentService
import io.pleo.antaeus.context.payment.RequestInvoicePaymentCommand
import io.vertx.core.AbstractVerticle

class CommandHandlerVerticle(private val invoiceService: InvoiceService,
                             private val invoicePaymentService: InvoicePaymentService,
                             private val billingService: BillingService) : AbstractVerticle() {

    override fun start() {
        vertx.eventBus().consumer<PayInvoiceCommand>("RequestBillingCommand") {
            invoiceService.on(it.body())
        }

        vertx.eventBus().consumer<RequestInvoicePaymentCommand>("RequestInvoicePaymentCommand") {
            invoicePaymentService.on(it.body())
        }

        vertx.eventBus().consumer<CompleteInvoicePaymentCommand>("CompleteInvoicePaymentCommand") {
            invoicePaymentService.on(it.body())
        }

        vertx.eventBus().consumer<CancelInvoicePaymentCommand>("CancelInvoicePaymentCommand") {
            invoicePaymentService.on(it.body())
        }

        vertx.eventBus().consumer<StartBillingBatchProcessCommand>("StartBillingBatchProcessCommand") {
            billingService.on(it.body())
        }

        vertx.eventBus().consumer<CompleteBillingBatchProcessCommand>("CompleteBillingBatchProcessCommand") {
            billingService.on(it.body())
        }
    }

    // Optional - called when verticle is undeployed
    override fun stop() {
    }
}