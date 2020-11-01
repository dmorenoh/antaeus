package io.pleo.antaeus.repository

import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentRepository
import io.pleo.antaeus.model.PaymentTable
import io.pleo.antaeus.model.toPayment
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class ExposedPaymentRepository(private val db: Database) : PaymentRepository {
    override fun save(payment: Payment): Payment? {
        transaction(db) {
            PaymentTable
                    .insert {
                        it[id] = payment.transactionId
                        it[invoiceId] = payment.invoiceId
                        it[status] = payment.status.toString()
                        it[cancellationReason] = payment.paymentCancellationReason.toString()
                        it[billingProcessId] = payment.billingProcessId
                    }
        }
        return load(payment.transactionId)
    }

    override fun update(transaction: Payment) {
        transaction(db) {
            PaymentTable
                    .update({ PaymentTable.id eq transaction.transactionId }) {
                        it[invoiceId] = transaction.invoiceId
                        it[status] = transaction.status.toString()
                        it[cancellationReason] = transaction.paymentCancellationReason.toString()
                        it[billingProcessId] = transaction.billingProcessId
                    }
        }
    }

    override fun load(transactionId: UUID): Payment? {
        return transaction(db) {
            PaymentTable
                    .select { PaymentTable.id.eq(transactionId) }
                    .firstOrNull()
                    ?.toPayment()
        }
    }
}