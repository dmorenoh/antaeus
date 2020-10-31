package io.pleo.antaeus.context.customer

interface CustomerRepository {
    fun fetchAll(): List<Customer>
    fun fetch(id: Int): Customer?
}