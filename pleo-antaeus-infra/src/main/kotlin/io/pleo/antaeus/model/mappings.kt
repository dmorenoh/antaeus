package io.pleo.antaeus.model

import io.pleo.antaeus.context.billing.Billing
import io.pleo.antaeus.context.billing.BillingStatus
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.Payment
import io.pleo.antaeus.context.payment.PaymentCancellationReason
import io.pleo.antaeus.context.payment.PaymentStatus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInvoice(): Invoice = Invoice(
        id = this[InvoiceTable.id],
        amount = Money(
                value = this[InvoiceTable.value],
                currency = Currency.valueOf(this[InvoiceTable.currency])
        ),
        status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
        customerId = this[InvoiceTable.customerId],
        version = this[InvoiceTable.version]
)

fun ResultRow.toPayment(): Payment = Payment(
        transactionId = this[PaymentTable.id],
        invoiceId = this[PaymentTable.invoiceId],
        status = PaymentStatus.valueOf(this[PaymentTable.status]),
        paymentCancellationReason = PaymentCancellationReason.valueOf(this[PaymentTable.cancellationReason]),
        billingProcessId = this[PaymentTable.billingProcessId]
)

fun ResultRow.toCustomer(): Customer = Customer(
        id = this[CustomerTable.id],
        currency = Currency.valueOf(this[CustomerTable.currency])
)

fun ResultRow.toBilling(): Billing = Billing(
        processId = this[BillingsTable.id],
        status = BillingStatus.valueOf(this[BillingsTable.status])
)

fun ResultRow.toBilling(payments: List<Payment>): Billing = Billing(
        processId = this[BillingsTable.id],
        status = BillingStatus.valueOf(this[BillingsTable.status]),
        payments = payments
)