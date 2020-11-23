package io.pleo.antaeus.repository

import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentRepository
import java.util.*

class InMemoryPaymentRepository(private val paymentsMap: MutableMap<UUID, Payment?> = mutableMapOf()) : PaymentRepository {
    override fun save(payment: Payment): Payment? {
        paymentsMap[payment.transactionId] = payment
        return payment
    }


    override fun update(payment: Payment): Payment {
        paymentsMap.replace(payment.transactionId, payment)
        return payment
    }


    override fun load(transactionId: UUID): Payment? {
        return paymentsMap[transactionId]
    }

}