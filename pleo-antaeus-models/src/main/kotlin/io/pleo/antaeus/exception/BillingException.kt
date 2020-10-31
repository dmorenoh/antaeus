package io.pleo.antaeus.exception

import java.lang.RuntimeException

class BillingPendingPaymentsException(id: Int) : RuntimeException("Billing '$id' has pending payments to be processed")