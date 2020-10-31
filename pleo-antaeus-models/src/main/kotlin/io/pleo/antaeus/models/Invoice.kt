package io.pleo.antaeus.models

data class Invoice(
        val id: Int,
        val customerId: Int,
        val amount: Money,
        val status: InvoiceStatus,
        val version: Int
) {
    fun currency(): Currency {
        return amount.currency
    }

    fun isPending(): Boolean {
        return status == InvoiceStatus.PENDING
    }

    fun pay(): Invoice {
        return copy(status = InvoiceStatus.PAID)
    }

    fun pay(provider: PaymentProvider): Invoice {
        if (status == InvoiceStatus.PAID)
            throw InvalidInvoiceStatusException("Invalid")
        if (!provider.charge(this)) {
            throw AccountBalanceException("payment not allowed for InvoiceId:${id}")
        }
        return this
    }

}
