package io.pleo.antaeus.context.payment


import io.pleo.antaeus.context.invoice.AccountBalanceException
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.PaymentFailedEvent
import io.pleo.antaeus.context.payment.PaymentCancellationReason.*
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.CommandBus
import io.vertx.core.eventbus.EventBus
import mu.KotlinLogging


class PaymentSagaOld(private val commandBus: CommandBus, private val eventBus: EventBus,
                     private val repository: InvoiceRepository,
                     private val paymentProvider: PaymentProvider) {

    private val logger = KotlinLogging.logger {}

//    fun on(event: PaymentRequestedEvent) {
//
//        commandBus.send(PayInvoiceCommand(event.transactionId, event.invoiceId))
//    }



    fun on(event: PaymentFailedEvent) {
        logger.info { "PaymentFailedEvent :${event.invoiceId}" }
        commandBus.send(CancelPaymentCommand(
                transactionId = event.transactionId,
                invoiceId = event.invoiceId,
                paymentCancellationReason = cancellationReason(event.throwable))
        )
    }

    fun on(event: InvoicePaidEvent) {
        logger.info { "InvoicePaidEvent :${event.invoiceId}" }
        commandBus.send(CompletePaymentCommand(event.transactionId, event.invoiceId))
    }

    private fun cancellationReason(throwable: Throwable): PaymentCancellationReason {
        return when (throwable) {
            is InvalidInvoiceStatusException -> INVALID_INVOICE_STATUS
            is CurrencyMismatchException -> CURRENCY_MISMATCH
            is CustomerNotFoundException -> CUSTOMER_NOT_FOUND
            is NetworkException -> NETWORK_FAILED
            is AccountBalanceException -> ACCOUNT_BALANCE_ISSUE
            else -> UNKNOWN
        }
    }
}

