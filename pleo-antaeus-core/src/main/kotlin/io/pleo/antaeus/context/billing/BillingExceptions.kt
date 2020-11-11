package io.pleo.antaeus.context.billing

class InvalidBillingTransactionException(message: String) :
        RuntimeException(message)
class BillingPendingPaymentsException(id: String) :
        RuntimeException("Billing '$id' has pending payments to be processed")
class BillingStatusException(id: String):
        RuntimeException("Billing incompatible status'$id' has pending payments to be processed")