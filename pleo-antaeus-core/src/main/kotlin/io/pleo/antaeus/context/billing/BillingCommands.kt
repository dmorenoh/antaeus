package io.pleo.antaeus.context.billing

import io.pleo.antaeus.core.commands.Command
import java.util.*

data class StartBillingCommand(val processId: UUID) : Command
data class CompleteBillingCommand(val processId: UUID) : Command