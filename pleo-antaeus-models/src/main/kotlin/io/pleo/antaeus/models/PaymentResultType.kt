package io.pleo.antaeus.models

enum class PaymentResultType {
    SUCCEED,
    NO_BALANCE_ACCOUNT,
    CURRENCY_MISMATCH,
    CUSTOMER_NOT_FOUND,
    NETWORK_FAILED,
    UNKNOWN
}