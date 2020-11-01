/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.context.customer

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException

class CustomerService(private val repository: CustomerRepository) {

    fun fetchAll(): List<Customer> {
        return repository.fetchAll()
    }

    fun fetch(id: Int): Customer {
        return repository.load(id) ?: throw CustomerNotFoundException(id)
    }
}
