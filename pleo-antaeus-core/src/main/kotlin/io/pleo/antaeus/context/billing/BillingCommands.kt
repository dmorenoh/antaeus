package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class StartBillingCommand(val invoicesIds: List<Int>) : Command
data class CloseBillingInvoiceCommand(val billingId: UUID, val invoicesId: Int) : Command
