package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.payment.external.CurrencyExchangeProvider
import io.pleo.antaeus.context.payment.external.MoneyExchangeRequest
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money

data class Invoice(
        val id: Int,
        val customer: Customer,
        val amount: Money,
        val status: InvoiceStatus,
        val version: Int
) {

    fun currency(): Currency = amount.currency

    fun pay(provider: PaymentProvider): Invoice {

        if (status == InvoiceStatus.PAID)
            throw InvalidInvoiceStatusException("Invalid")

        if (mismatchCustomerCurrency())
            throw CurrencyMismatchException(id, customer.id)

        if (!provider.charge(this)) {
            throw AccountBalanceException("payment not allowed for InvoiceId:${id}")
        }
        return copy(status = InvoiceStatus.PAID)
    }


    private fun mismatchCustomerCurrency(): Boolean {
        return amount.currency != customer.currency
    }

    fun fixCurrency(provider: CurrencyExchangeProvider): Invoice {
        if (!mismatchCustomerCurrency()) {
            throw InvalidInvoiceStatusException("Invalid")
        }
        val fixedAmount = provider.convert(MoneyExchangeRequest(this.amount, this.customer.currency))
        return copy(amount = fixedAmount)
    }
}