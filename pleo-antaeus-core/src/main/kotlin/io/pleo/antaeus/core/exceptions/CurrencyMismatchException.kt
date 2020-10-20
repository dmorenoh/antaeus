package io.pleo.antaeus.core.exceptions

class CurrencyMismatchException(invoiceId: Int, customerId: Int) :
    RuntimeException("Currency of invoice '$invoiceId' does not match currency of customer '$customerId'")
