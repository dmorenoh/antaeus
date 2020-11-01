package io.pleo.antaeus.repository

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.customer.CustomerRepository
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.toCustomer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedCustomerRepository(private val db: Database) :CustomerRepository{
    override fun createCustomer(currency: Currency): Customer? {
                val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.currency] = currency.toString()
            } get CustomerTable.id
        }
        return load(id)
    }

    override fun fetchAll(): List<Customer> {
                return transaction(db) {
            CustomerTable
                    .selectAll()
                    .map { it.toCustomer() }
        }
    }

    override fun load(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                    .select { CustomerTable.id.eq(id) }
                    .firstOrNull()
                    ?.toCustomer()
        }
    }

}