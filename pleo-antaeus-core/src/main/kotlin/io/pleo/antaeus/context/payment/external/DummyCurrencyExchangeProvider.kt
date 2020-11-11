package io.pleo.antaeus.context.payment.external

import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import java.math.BigDecimal

class DummyCurrencyExchangeProvider : CurrencyExchangeProvider {
    override fun convert(request: MoneyExchangeRequest): Money {
        if (request.originAmount.currency == request.finalCurrency)
            return request.originAmount
        return when (request.finalCurrency) {
            Currency.EUR -> convertToEuros(request.originAmount)
            else -> request.originAmount
        }
    }

    private fun convertToEuros(amount: Money): Money {
        val multiplier = when (amount.currency) {
            Currency.USD -> BigDecimal("0.85")
            Currency.DKK -> BigDecimal("0.13")
            Currency.GBP -> BigDecimal("1.11")
            Currency.SEK -> BigDecimal("0.098")
            else -> BigDecimal("1.00")
        }
        val newValue = amount.value.multiply(multiplier)
        return Money(newValue, Currency.EUR)
    }
}