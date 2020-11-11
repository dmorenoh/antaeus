package io.pleo.antaeus.context.payment.external

import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money

data class MoneyExchangeRequest(val originAmount: Money, val finalCurrency: Currency)