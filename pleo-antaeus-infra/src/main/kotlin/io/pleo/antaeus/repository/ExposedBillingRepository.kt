package io.pleo.antaeus.repository

import io.pleo.antaeus.context.billing.Billing
import io.pleo.antaeus.context.billing.BillingRepository
import io.pleo.antaeus.context.billing.BillingStatus
import io.pleo.antaeus.model.BillingsTable
import io.pleo.antaeus.model.PaymentTable
import io.pleo.antaeus.model.toBilling
import io.pleo.antaeus.model.toPayment
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


class ExposedBillingRepository(private val db: Database) : BillingRepository {
    override fun save(billing: Billing): Billing? {
        transaction(db) {
            BillingsTable
                    .insert {
                        it[id] = billing.processId
                        it[status] = BillingStatus.STARTED.name
                    }
        }
        return load(billing.processId)
    }

    override fun update(billing: Billing) {
        transaction(db) {
            BillingsTable
                    .update({ BillingsTable.id eq billing.processId }) {
                        it[status] = billing.status.name
                    }
        }
    }

    override fun load(processId: UUID): Billing? {
        return transaction(db) {
            BillingsTable
                    .select { BillingsTable.id.eq(processId) }
                    .firstOrNull()
                    ?.toBilling(PaymentTable
                            .select { PaymentTable.billingProcessId.eq(processId) }
                            .map { it.toPayment() })
        }
    }
}