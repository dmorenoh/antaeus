package io.pleo.antaeus.context.payment


import io.pleo.antaeus.context.invoice.command.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.exception.AccountBalanceException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.models.PaymentFailureReason

class InvoicePaymentSagaService(private val commandBus: CommandBus) {

    fun handle(event: InvoicePaymentRequestedEvent) {
        commandBus.send(PayInvoiceCommand(event.transactionId, event.invoiceId)).exceptionally {
            commandBus.send(CancelInvoicePaymentCommand(
                    transactionId = event.transactionId,
                    invoiceId = event.invoiceId,
                    paymentFailureReason = getPaymentFailureReason(it))
            )
            return@exceptionally null
        }
    }

    private fun getPaymentFailureReason(throwable: Throwable): PaymentFailureReason {
        return when (throwable) {
            is InvalidInvoiceStatusException -> PaymentFailureReason.INVALID_INVOICE_STATUS
            is CurrencyMismatchException -> PaymentFailureReason.CURRENCY_MISMATCH
            is CustomerNotFoundException -> PaymentFailureReason.CUSTOMER_NOT_FOUND
            is NetworkException -> PaymentFailureReason.NETWORK_FAILED
            is AccountBalanceException -> PaymentFailureReason.ACCOUNT_BALANCE_ISSUE
            else -> PaymentFailureReason.UNKNOWN
        }
    }
}