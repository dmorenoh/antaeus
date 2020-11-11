package io.pleo.antaeus.repository

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.customer.CustomerRepository
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.entities.CustomerEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedCustomerRepository(private val db: Database) : CustomerRepository {
    override fun createCustomer(currency: Currency): Customer? {
        return transaction(db) {
            return@transaction CustomerEntity.new {
                this.currency = currency.toString()
            }.toCustomer()
        }
    }

    override fun fetchAll(): List<Customer> {
        return transaction(db) {
            CustomerEntity.all().map { it.toCustomer() }
        }
    }

    override fun load(id: Int): Customer? {
        return transaction(db) {
            CustomerEntity.findById(id)?.toCustomer()
        }
    }

}