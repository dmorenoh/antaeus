package io.pleo.antaeus.context.invoice

import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.value.Money

class Invoice(
        val id: Int,
        val customerId: Int,
        val amount: Money,
        val status: InvoiceStatus,
        val version: Int
) {
    fun pay(provider: PaymentProvider): Invoice {
        if (status == InvoiceStatus.PAID)
            throw InvalidInvoiceStatusException("Invalid")
        if (!provider.charge(this)) {
            throw AccountBalanceException("payment not allowed for InvoiceId:${id}")
        }
        return this
    }
}