package io.pleo.antaeus.models

enum class PaymentFailureReason {
    NONE,
    INVALID_INVOICE_STATUS,
    ACCOUNT_BALANCE_ISSUE,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUND,
    NETWORK_FAILED,
    UNKNOWN;

}