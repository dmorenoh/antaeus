package io.pleo.antaeus.data

import io.pleo.antaeus.context.payment.InvoicePaymentDal
import io.pleo.antaeus.models.InvoicePaymentTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

class InvoicePaymentDalImpl(private val db: Database) : InvoicePaymentDal {
    override fun save(transaction: InvoicePaymentTransaction) {
        transaction(db) {
            InvoicePaymentTransactionTable
                    .insert {
                        it[invoiceId] = transaction.invoiceId
                        it[status] = transaction.status.toString()
                        it[cancellationReason] = transaction.cancellationReason.toString()
                        it[billingProcessId] = transaction.billingProcessId
                    }
        }
    }

    override fun update(transaction: InvoicePaymentTransaction) {
        transaction(db) {
            InvoicePaymentTransactionTable
                    .update({ InvoicePaymentTransactionTable.id eq transaction.transactionId }) {
                        it[invoiceId] = transaction.invoiceId
                        it[status] = transaction.status.toString()
                        it[cancellationReason] = transaction.cancellationReason.toString()
                        it[billingProcessId] = transaction.billingProcessId
                    }
        }
    }

    override fun fetch(transactionId: UUID): InvoicePaymentTransaction? {
        return transaction(db) {
            InvoicePaymentTransactionTable
                    .select { InvoicePaymentTransactionTable.id.eq(transactionId) }
                    .firstOrNull()
                    ?.toInvoicePaymentTransaction()
        }
    }

    override fun fetchByProcessId(billingProcessId: Int): List<InvoicePaymentTransaction> {
        return transaction(db) {
            InvoicePaymentTransactionTable
                    .select { InvoicePaymentTransactionTable.billingProcessId.eq(billingProcessId) }
                    .map { it.toInvoicePaymentTransaction() }
        }
    }
}