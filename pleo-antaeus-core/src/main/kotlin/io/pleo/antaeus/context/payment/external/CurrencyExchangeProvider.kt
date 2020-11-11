package io.pleo.antaeus.context.payment.external

import io.pleo.antaeus.core.value.Money

interface CurrencyExchangeProvider {
    fun convert(request: MoneyExchangeRequest): Money
}