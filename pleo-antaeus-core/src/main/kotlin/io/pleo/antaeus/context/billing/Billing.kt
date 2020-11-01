package io.pleo.antaeus.context.billing

import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentStatus
import java.util.*

data class Billing(val processId: UUID,
                   val status: BillingStatus = BillingStatus.STARTED,
                   val payments: List<Payment> = emptyList()
) {
    fun complete(): Billing {
        if (payments.any { it.status == PaymentStatus.STARTED })
            throw BillingPendingPaymentsException(processId.toString())
        return copy(status = BillingStatus.COMPLETED)
    }

    companion object {
        fun create(uuid: UUID): Billing {
            return Billing(uuid, status = BillingStatus.STARTED)
        }
    }
}