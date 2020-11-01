package io.pleo.antaeus.context.customer

import io.pleo.antaeus.core.value.Currency

interface CustomerRepository {
    fun createCustomer(currency: Currency):Customer?
    fun fetchAll(): List<Customer>
    fun load(id: Int): Customer?
}