package io.pleo.antaeus.context.customer

import io.pleo.antaeus.core.value.Currency

data class Customer(
    val id: Int,
    val currency: Currency
)
