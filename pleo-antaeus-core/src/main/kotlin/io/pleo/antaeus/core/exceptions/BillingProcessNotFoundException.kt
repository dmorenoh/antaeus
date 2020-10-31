package io.pleo.antaeus.core.exceptions

class BillingProcessNotFoundException(id: String) : RuntimeException("Batch Process '${id}' not found")