package io.pleo.antaeus.app.saga

import io.pleo.antaeus.context.invoice.*
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging

class PaymentSaga(private val commandBus: CommandBus) {

    private val logger = KotlinLogging.logger {}

    fun on(event: PaymentCreatedEvent) {
        commandBus.send(PayInvoiceCommand(event.transactionId, event.invoiceId))
    }

    fun on(event: PaymentFailedEvent) {
        logger.info { "PaymentFailedEvent :${event.invoiceId}" }
        if (event.throwable is CurrencyMismatchException)
            commandBus.send(FixCurrencyInvoiceCommand(event.invoiceId))
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

    fun on(event: InvoiceCurrencyUpdatedEvent) {
        logger.info { "Reprocess payment :${event.invoiceId}" }
        commandBus.send(CreatePaymentCommand(event.invoiceId))
    }

    private fun cancellationReason(throwable: Throwable): PaymentCancellationReason {
        return when (throwable) {
            is InvalidInvoiceStatusException -> PaymentCancellationReason.INVALID_INVOICE_STATUS
            is CurrencyMismatchException -> PaymentCancellationReason.CURRENCY_MISMATCH
            is CustomerNotFoundException -> PaymentCancellationReason.CUSTOMER_NOT_FOUND
            is NetworkException -> PaymentCancellationReason.NETWORK_FAILED
            is AccountBalanceException -> PaymentCancellationReason.ACCOUNT_BALANCE_ISSUE
            else -> PaymentCancellationReason.UNKNOWN
        }
    }
}