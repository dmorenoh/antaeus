package io.pleo.antaeus.core.commands

import io.pleo.antaeus.models.Invoice

data class RequestBillingCommand (val invoice: Invoice)
