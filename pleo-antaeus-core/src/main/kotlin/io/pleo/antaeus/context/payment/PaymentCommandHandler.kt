package io.pleo.antaeus.context.payment

import io.pleo.antaeus.core.exceptions.InvoicePaymentNotFoundException
import io.pleo.antaeus.core.messagebus.EventBus

class PaymentCommandHandler(private val repository: PaymentRepository,
                            private val eventBus: EventBus) {


    fun handle(command: RequestPaymentCommand) {

        val payment = repository.save(Payment.create(command))
                ?: throw InvalidPaymentException("no possible to be created")

        eventBus.publish(PaymentRequestedEvent(
                payment.transactionId,
                payment.invoiceId,
                payment.billingProcessId))
    }

    fun handle(command: CompletePaymentCommand) {

        val invoicePayment = repository.load(command.transactionId)
                ?: throw InvoicePaymentNotFoundException("Invoice Payment not found with Id:'${command.transactionId}'")

        repository.update(invoicePayment.complete())

        eventBus.publish(PaymentCompletedEvent(
                invoicePayment.transactionId,
                invoicePayment.invoiceId,
                invoicePayment.billingProcessId))

    }

    fun handle(command: CancelPaymentCommand) {

        val invoicePayment = repository.load(command.transactionId)
                ?: throw InvoicePaymentNotFoundException("Invoice Payment not found with Id:'${command.transactionId}'")

        repository.update(invoicePayment.cancel(command.paymentCancellationReason))

        eventBus.publish(PaymentCanceledEvent(
                invoicePayment.transactionId,
                invoicePayment.invoiceId,
                command.paymentCancellationReason,
                invoicePayment.billingProcessId))

    }

}