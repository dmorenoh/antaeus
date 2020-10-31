package io.pleo.antaeus.context.payment


import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.AccountBalanceException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.CommandBus

class PaymentSaga(private val commandBus: CommandBus) {

    fun on(event: PaymentRequestedEvent) {
        commandBus.send(PayInvoiceCommand(event.transactionId, event.invoiceId)).exceptionally {
            commandBus.send(CancelPaymentCommand(
                    transactionId = event.transactionId,
                    invoiceId = event.invoiceId,
                    paymentCancellationReason = cancellationReason(it))
            )
            return@exceptionally null
        }
    }

    fun on(event: InvoicePaidEvent) {
        commandBus.send(CompletePaymentCommand(event.transactionId, event.invoiceId))
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