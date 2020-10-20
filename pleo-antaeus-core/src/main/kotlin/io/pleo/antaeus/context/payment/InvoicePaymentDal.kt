package io.pleo.antaeus.context.payment

import io.pleo.antaeus.models.InvoicePaymentTransaction

interface InvoicePaymentDal {
    fun save(transaction: InvoicePaymentTransaction)
    fun update(transaction: InvoicePaymentTransaction)
    fun fetch(transactionId: String): InvoicePaymentTransaction
}