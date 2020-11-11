package io.pleo.antaeus.entities

import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.model.CustomerTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.EntityID

class CustomerEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, CustomerEntity>(CustomerTable)

    var currency by CustomerTable.currency

    fun toCustomer(): Customer = Customer(
            id = this.id.value,
            currency = Currency.valueOf(this.currency)
    )
}