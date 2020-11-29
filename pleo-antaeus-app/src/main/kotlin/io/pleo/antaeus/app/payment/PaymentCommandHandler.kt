package io.pleo.antaeus.app.payment

import arrow.core.Either
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.core.event.Event

class PaymentCommandHandler(private val paymentService: PaymentService,
                            private val invoiceService: InvoiceService) {

    suspend fun handle(command: PaymentCommand): Either<Throwable, Event> = when (command) {
        is CreatePaymentCommand -> paymentService.execute(command)
        is PayInvoiceCommand -> invoiceService.execute(command)
        is ChargeInvoiceCommand -> invoiceService.execute(command)
        is CompletePaymentCommand -> paymentService.execute(command)
        is RevertPaymentCommand -> invoiceService.execute(command)
        is CancelPaymentCommand -> paymentService.execute(command)
        else -> Either.left(RuntimeException("Invalid command ${command::class.simpleName}"))
    }

}