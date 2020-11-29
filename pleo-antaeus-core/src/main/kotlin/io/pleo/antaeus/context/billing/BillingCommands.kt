package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.commands.Command
import java.util.*
interface BillingCommand : Command
data class StartBillingCommand(val invoicesIds: List<Int>) : BillingCommand
data class CloseBillingInvoiceCommand(val billingId: UUID, val invoicesId: Int) : BillingCommand
