/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.context.invoice.service

import io.pleo.antaeus.context.invoice.command.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.dal.InvoiceDal
import io.pleo.antaeus.context.invoice.event.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.exception.AccountBalanceException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.Invoice

class InvoiceService(private val dal: InvoiceDal,
                     private val paymentProvider: PaymentProvider,
                     private val eventBus: EventBus) {

    fun on(command: PayInvoiceCommand) {

        val pendingInvoice = dal.fetchInvoice(command.invoiceId)
                .takeIf { it.isPending() }
                ?: throw InvalidInvoiceStatusException("not valid status for InvoiceId:${command.invoiceId}")

        if (!paymentProvider.charge(pendingInvoice)) {
            throw AccountBalanceException("payment not allowed for InvoiceId:${command.invoiceId}")
        }

        dal.update(pendingInvoice.paid())
        eventBus.publish(InvoicePaidEvent(command.transactionId, pendingInvoice.id))
    }


    fun fetchAllPending(): List<Invoice> {
        return dal.fetchInvoices()
                .filter(Invoice::isPending)
    }

    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }
}
