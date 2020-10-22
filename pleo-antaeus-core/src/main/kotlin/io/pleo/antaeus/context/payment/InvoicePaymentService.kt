package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.InvoicePaymentTransaction

class InvoicePaymentService(private val invoicePaymentDal: InvoicePaymentDal,
                            private val eventBus: EventBus) {

    fun on(command: RequestInvoicePaymentCommand) {

        invoicePaymentDal.save(InvoicePaymentTransaction(
                transactionId = command.transactionId,
                invoiceId = command.invoiceId,
                billingProcessId = command.processId))

        eventBus.publish(InvoicePaymentRequestedEvent(command.transactionId, command.invoiceId, command.processId))
    }

    fun on(command: CompleteInvoicePaymentCommand) {

        val invoicePayment = invoicePaymentDal.fetch(command.transactionId)

        invoicePaymentDal.update(invoicePayment.complete())

        eventBus.publish(InvoicePaymentCompletedEvent(
                invoicePayment.transactionId,
                invoicePayment.invoiceId,
                invoicePayment.billingProcessId))

    }

    fun on(command: CancelInvoicePaymentCommand) {

        val invoicePayment = invoicePaymentDal.fetch(command.transactionId)

        invoicePaymentDal.update(invoicePayment.canceled(command.paymentFailureReason))

        eventBus.publish(InvoicePaymentCanceledEvent(
                invoicePayment.transactionId,
                invoicePayment.invoiceId,
                command.paymentFailureReason,
                invoicePayment.billingProcessId))

    }

    fun fetchByBillingProcessId(billingProcessId: Int): List<InvoicePaymentTransaction> {

        return emptyList()
    }
}