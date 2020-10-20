package io.pleo.antaeus.context.payment

import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.PaymentResultType

class PaymentService(
        private val invoiceService: InvoiceService,
        private val paymentProvider: PaymentProvider) {

    fun on(command: RequestInvoicePaymentCommand) {
        val pendingInvoice = (invoiceService.fetch(command.invoiceId)
                .takeIf { it.isPending() }
                ?: throw InvalidInvoiceStatusException("not valid status"))

        charge(pendingInvoice)

    }

    fun charge(invoice: Invoice): PaymentResultType {
        try {
            val charged = paymentProvider.charge(invoice)
            if (charged) {
                return PaymentResultType.SUCCEED
            }
            return PaymentResultType.NO_BALANCE_ACCOUNT
        } catch (e: Exception) {
            if (e is CurrencyMismatchException) {
                return PaymentResultType.CURRENCY_MISMATCH
            }
            if (e is CustomerNotFoundException) {
                return PaymentResultType.CUSTOMER_NOT_FOUND
            }
            if (e is NetworkException) {
                return PaymentResultType.NETWORK_FAILED
            }
            return PaymentResultType.UNKNOWN
        }
    }
}