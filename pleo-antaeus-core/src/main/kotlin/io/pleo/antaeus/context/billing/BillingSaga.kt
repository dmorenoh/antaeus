package io.pleo.antaeus.context.billing

import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.context.payment.RequestPaymentCommand
import io.pleo.antaeus.core.messagebus.CommandBus
import java.util.*

class BillingSaga(private val invoiceService: InvoiceService,
                  private val commandBus: CommandBus
) {
    fun handle(event: BillingRequestedEvent) {

        val pendingInvoices = invoiceService.fetchAllPending()

        if (pendingInvoices.isEmpty()) {
            commandBus.send(CompleteBillingCommand(event.processId))
            return
        }

        pendingInvoices.forEach { invoice ->
                    commandBus.send(command = RequestPaymentCommand(
                            invoiceId = invoice.id,
                            processId = event.processId)
                    )
                }
    }

    fun handle(event: PaymentCanceledEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CompleteBillingCommand(event.billingProcessId!!)) }
    }

    fun handle(event: PaymentCompletedEvent) {
        event.billingProcessId
                ?.let { commandBus.send(CompleteBillingCommand(event.billingProcessId!!)) }
    }
}