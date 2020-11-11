package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class StartBillingCommand(val processId: UUID, val invoicesIds: List<Int>) : Command
data class CloseBillingInvoiceCommand(val processId: UUID, val invoicesId: Int) : Command
data class CompleteBillingCommand(val processId: UUID) : Command