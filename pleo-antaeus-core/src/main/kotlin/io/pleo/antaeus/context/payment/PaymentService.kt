package io.pleo.antaeus.context.payment

import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.CommandBus

class PaymentService(
        private val invoiceRepository: InvoiceRepository,
        private val commandBus: CommandBus) {

    suspend fun payInvoice(invoiceId: Int) {

        val invoice = invoiceRepository.load(invoiceId)
                ?.takeIf { it.status == InvoiceStatus.PENDING }
                ?: throw throw InvoiceNotFoundException(invoiceId)

        commandBus.sendAwait(CreatePaymentCommand(invoice.id))
    }
}