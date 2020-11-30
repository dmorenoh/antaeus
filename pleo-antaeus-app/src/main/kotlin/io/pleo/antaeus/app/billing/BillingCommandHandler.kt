package io.pleo.antaeus.app.billing

import arrow.core.Either
import io.pleo.antaeus.context.billing.BillingService
import io.pleo.antaeus.context.billing.CloseBillingInvoiceCommand
import io.pleo.antaeus.context.billing.StartBillingCommand
import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.core.event.Event

class BillingCommandHandler(private val billingService: BillingService) {

    suspend fun handle(command: Command): Either<Throwable, Event> = when (command) {
        is StartBillingCommand -> billingService.execute(command)
        is CloseBillingInvoiceCommand -> billingService.execute(command)
        else -> Either.left(RuntimeException("Invalid command ${command::class.simpleName}"))
    }


}
