/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.context.invoice

import arrow.core.Either
import io.pleo.antaeus.context.payment.ChargeInvoiceCommand
import io.pleo.antaeus.context.payment.PaymentNotFoundException
import io.pleo.antaeus.context.payment.RevertPaymentCommand
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import mu.KotlinLogging

class InvoiceService(private val repository: InvoiceRepository,
                     private val paymentProvider: PaymentProvider) {
    private val logger = KotlinLogging.logger {}

    suspend fun execute(command: PayInvoiceCommand): Either<Throwable, Event> = Either.catch {

        val invoice = repository.loadAsync(command.invoiceId)
                ?: throw InvoiceNotFoundException(command.invoiceId)

        invoice.pay()
                .also { repository.updateAsync(it) }
                .let {
                    InvoicePaidEvent(transactionId = command.transactionId, invoiceId = it.id, it.status)
                }
    }

    suspend fun execute(command: ChargeInvoiceCommand): Either<Throwable, Event> = Either.catch {

        val invoice = repository.loadAsync(command.invoiceId)
                ?.takeIf { invoice -> invoice.isPaid() }
                ?: throw InvoiceNotFoundException(command.invoiceId)

        if (!paymentProvider.charge(invoice)) throw AccountBalanceException("No money")

        InvoiceChargedEvent(command.transactionId, command.invoiceId)
    }


    suspend fun execute(command: RevertPaymentCommand): Either<Throwable, Event> = Either.catch {
        val invoice = repository.loadAsync(command.invoiceId)
                ?: throw PaymentNotFoundException("Payment not found ${command.transactionId}")

        invoice.revertPayment()
                .also { repository.updateAsync(it) }
                .let {
                    PaymentRevertedEvent(
                            transactionId = command.transactionId,
                            invoiceId = it.id,
                            invoiceStatus = it.status,
                            reason = command.reason)
                }
    }

    fun fetchAllPending(): List<Invoice> {
        return repository.fetchByStatus(InvoiceStatus.PENDING)
    }

    fun fetchAll(): List<Invoice> {
        return repository.fetchAll()
    }

    fun fetch(id: Int): Invoice {
        return repository.load(id) ?: throw InvoiceNotFoundException(id)
    }

    suspend fun charge(invoiceId: Int) {
        repository.loadAsync(invoiceId)
                ?.let { invoice -> paymentProvider.charge(invoice) }
                .takeIf { it == false }
                ?.apply { throw AccountBalanceException("No money") }
                ?.run { logger.info { "Charged!" } }

    }
}
