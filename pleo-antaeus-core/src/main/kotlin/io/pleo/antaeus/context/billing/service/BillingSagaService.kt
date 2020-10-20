package io.pleo.antaeus.context.billing.service

import io.pleo.antaeus.context.billing.command.CompleteBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.event.BillingBatchProcessStartedEvent
import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.context.payment.InvoicePaymentCanceledEvent
import io.pleo.antaeus.context.payment.InvoicePaymentCompletedEvent
import io.pleo.antaeus.context.payment.RequestInvoicePaymentCommand
import io.pleo.antaeus.core.messagebus.CommandBus
import java.util.*

class BillingSagaService(private val invoiceService: InvoiceService,
                         private val commandBus: CommandBus
) {
    fun handle(event: BillingBatchProcessStartedEvent) {

        val pendingInvoices = invoiceService.fetchAllPending()

        if (pendingInvoices.isEmpty()) {
            commandBus.send(CompleteBillingBatchProcessCommand(event.processId))
            return
        }

        invoiceService.fetchAllPending()
                .forEach { invoice ->
                    commandBus.send(command = RequestInvoicePaymentCommand(
                            transactionId = UUID.randomUUID().toString(),
                            invoiceId = invoice.id,
                            processId = event.processId)
                    )
                }
    }

    fun handle(event: InvoicePaymentCanceledEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CompleteBillingBatchProcessCommand(event.billingProcessId!!)) }
    }

    fun handle(event: InvoicePaymentCompletedEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CompleteBillingBatchProcessCommand(event.billingProcessId!!)) }
    }
}