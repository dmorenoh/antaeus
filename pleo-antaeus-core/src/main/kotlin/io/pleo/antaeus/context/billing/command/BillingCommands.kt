package io.pleo.antaeus.context.billing.command

import io.pleo.antaeus.core.commands.Command

data class StartBillingBatchProcessCommand(val processId: Int) : Command
data class CompleteBillingBatchProcessCommand(val processId: Int) : Command