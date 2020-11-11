package io.pleo.antaeus.entities

import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import io.pleo.antaeus.model.InvoiceTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class InvoiceEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<InvoiceEntity>(InvoiceTable)

    var currency by InvoiceTable.currency
    var value by InvoiceTable.value
    var status by InvoiceTable.status
    var version by InvoiceTable.version
    var customer by CustomerEntity referencedOn InvoiceTable.customerId

    fun toInvoice(): Invoice = Invoice(
            id = this.id.value,
            customer = this.customer.toCustomer(),
            amount = Money(
                    value = this.value,
                    currency = Currency.valueOf(this.currency)),
            status = InvoiceStatus.valueOf(this.status),
            version = this.version

    )
}