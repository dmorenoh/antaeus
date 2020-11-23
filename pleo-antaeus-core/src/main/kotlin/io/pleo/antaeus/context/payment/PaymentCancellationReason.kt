package io.pleo.antaeus.context.payment

import io.pleo.antaeus.context.invoice.AccountBalanceException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException

enum class PaymentCancellationReason  {

    INVALID_INVOICE_STATUS,
    ACCOUNT_BALANCE_ISSUE,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUND,
    NETWORK_FAILED,
    UNKNOWN;

    companion object {
        @JvmStatic
        fun of(throwable: Throwable): PaymentCancellationReason {
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


}

