package io.pleo.antaeus.context.billing

import io.pleo.antaeus.context.payment.CreatePaymentCommand
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging

class BillingSaga(private val commandBus: CommandBus) {

    private val logger = KotlinLogging.logger {}

    fun on(event: BillingStartedEvent) = event.invoices.forEach { invoiceId ->
        commandBus.send(CreatePaymentCommand(invoiceId, event.billingId))
    }

    fun on(event: PaymentCompletedEvent) = event.takeIf { it.billingId != null }
        .let { commandBus.send(CloseBillingInvoiceCommand(event.billingId!!, event.invoiceId)) }


    fun on(event: PaymentCanceledEvent) {
        logger.info { "Received payment cancelled: ${event}" }
        event.takeIf { it.billingId != null }
            .let { commandBus.send(CloseBillingInvoiceCommand(event.billingId!!, event.invoiceId)) }
    }
}