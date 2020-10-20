package io.pleo.antaeus.context.billing.service

import io.pleo.antaeus.context.billing.command.CompleteBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.command.StartBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.dal.BillingDal
import io.pleo.antaeus.context.billing.event.BillingBatchProcessStartedEvent

import io.pleo.antaeus.context.billing.exceptions.InvalidBillingTransactionException
import io.pleo.antaeus.context.payment.InvoicePaymentService

import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.BillingBatchProcess
import io.pleo.antaeus.models.PaymentStatus

class BillingService(
        private val invoicePaymentService: InvoicePaymentService,
        private val billingDal: BillingDal,
        private val eventBus: EventBus
) {

    fun on(command: StartBillingBatchProcessCommand) {

        val billingProcess = billingDal.save(BillingBatchProcess(command.processId))
                ?: throw InvalidBillingTransactionException("no possible to be created")

        eventBus.publish(BillingBatchProcessStartedEvent(billingProcess.processId))
    }

    fun on(command: CompleteBillingBatchProcessCommand) {

        val billingProcess = billingDal.fetch(command.processId)

        invoicePaymentService.fetchByBillingProcessId(billingProcess.processId)
                .none { it.status == PaymentStatus.STARTED }
                .let { billingDal.update(billingProcess.complete()) }
    }
}
