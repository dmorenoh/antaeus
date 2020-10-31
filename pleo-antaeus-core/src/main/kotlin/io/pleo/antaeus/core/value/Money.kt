package io.pleo.antaeus.core.value

import java.math.BigDecimal

data class Money(
    val value: BigDecimal,
    val currency: Currency
)
