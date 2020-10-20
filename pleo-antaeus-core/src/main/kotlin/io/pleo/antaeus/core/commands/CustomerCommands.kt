package io.pleo.antaeus.core.commands

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Money

data class ValidateCustomerAccountCommand(var customerId: Int) : Command
data class ValidateCustomerCurrencyCommand(var customerId: Int, var currency: Currency) : Command
data class ValidateCustomerBalanceCommand(var customerId: Int, var debt: Money) : Command