package io.pleo.antaeus.context.payment

import java.util.*

interface PaymentRepository {
    fun save(payment: Payment):Payment?
    fun update(transaction: Payment)
    fun load(transactionId: UUID): Payment?
}