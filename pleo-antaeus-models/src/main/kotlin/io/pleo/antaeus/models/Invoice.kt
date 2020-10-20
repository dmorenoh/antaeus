package io.pleo.antaeus.models

data class Invoice(
        val id: Int,
        val customerId: Int,
        val amount: Money,
        val status: InvoiceStatus
) {
    fun currency(): Currency {
        return amount.currency
    }

    fun isPending(): Boolean {
        return status == InvoiceStatus.PENDING
    }

    fun paid(): Invoice {
        return copy(status = InvoiceStatus.PAID)
    }
}
