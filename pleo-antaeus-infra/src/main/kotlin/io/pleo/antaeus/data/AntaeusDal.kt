package io.pleo.antaeus.data

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import io.pleo.antaeus.entities.CustomerEntity
import io.pleo.antaeus.entities.InvoiceEntity
import io.pleo.antaeus.model.CustomerTable
import io.pleo.antaeus.model.InvoiceTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class AntaeusDal(private val db: Database) {

    fun createCustomer(currency: Currency): Customer? {
        return transaction(db) {
            CustomerEntity.new {
                this.currency = currency.toString()
            }.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerEntity.all()
                    .map { it.toCustomer() }
        }
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerEntity.findById(id)?.toCustomer()
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING): Invoice? {
        val id = transaction(db) {
            InvoiceTable
                    .insert {
                        it[this.value] = amount.value
                        it[this.currency] = amount.currency.toString()
                        it[this.status] = status.toString()
                        it[this.customerId] = EntityID(customer.id, CustomerTable)
                        it[this.version] = 1
                    } get InvoiceTable.id
        }

        return fetchInvoice(id.value)
    }

    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceEntity.findById(id)?.toInvoice()
        }
    }

    fun fetchAllInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceEntity.all()
                    .map { it.toInvoice() }
        }
    }

    fun fetchPendingInvoice(): List<Invoice> {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            InvoiceEntity.find { InvoiceTable.status.eq("PENDING") }
                    .map { it.toInvoice() }

        }
    }


}