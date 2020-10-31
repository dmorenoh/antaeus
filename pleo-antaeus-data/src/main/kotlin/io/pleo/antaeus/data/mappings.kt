/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.*
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

fun ResultRow.toInvoicePaymentTransaction(): InvoicePaymentTransaction = InvoicePaymentTransaction(
        transactionId = this[InvoicePaymentTransactionTable.id],
        invoiceId = this[InvoicePaymentTransactionTable.invoiceId],
        status = PaymentStatus.valueOf(this[InvoicePaymentTransactionTable.status]),
        cancellationReason = CancellationReason.valueOf(this[InvoicePaymentTransactionTable.cancellationReason]),
        billingProcessId = this[InvoicePaymentTransactionTable.billingProcessId]
)

fun ResultRow.toCustomer(): Customer = Customer(
        id = this[CustomerTable.id],
        currency = Currency.valueOf(this[CustomerTable.currency])
)

fun ResultRow.toBillingTransaction(): BillingBatchProcess = BillingBatchProcess(
        processId = this[BillingTransactionTable.id]
)