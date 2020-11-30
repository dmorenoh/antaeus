package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money

data class Invoice(
        val id: Int,
        val customer: Customer,
        val amount: Money,
        var status: InvoiceStatus,
        val version: Int
) {

    fun currency(): Currency = amount.currency

    fun pay(): Invoice {
        if (isPaid()) throw InvalidInvoiceStatusException("Invalid")
        if (mismatchCustomerCurrency()) throw CurrencyMismatchException(id, customer.id)
        return copy(status = InvoiceStatus.PAID)
    }

    fun revertPayment(): Invoice = copy(status = InvoiceStatus.PENDING)

    private fun mismatchCustomerCurrency(): Boolean = amount.currency != customer.currency

    fun isPaid(): Boolean = status == InvoiceStatus.PAID
}