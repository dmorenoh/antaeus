package io.pleo.antaeus.data

import io.pleo.antaeus.models.BillingStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class BillingDalImpl(private val db: Database)  {

    fun createBillingProcess(): Int? {
        return transaction(db) {
            BillingTransactionTable
                    .insert {
                        it[status] = BillingStatus.STARTED.name
                    } get BillingTransactionTable.id
        }
    }

    override fun update(billingBatchProcess: BillingBatchProcess) {
        transaction(db) {
            BillingTransactionTable
                    .update({ BillingTransactionTable.id eq billingBatchProcess.processId }) {
                        it[status] = billingBatchProcess.status.name
                    }
        }
    }


    override fun fetch(id: Int): BillingBatchProcess? {
        return transaction(db)

            val payments = InvoicePaymentTransactionTable
                    .select { InvoicePaymentTransactionTable.billingProcessId.eq(id) }
                    .map { it.toInvoicePaymentTransaction() }

            BillingTransactionTable
                    .select { BillingTransactionTable.id.eq(id) }
                    .firstOrNull()
                    ?.toBillingTransaction()


        }
    }

}