package io.pleo.antaeus.context.payment

import arrow.core.Either
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging

class PaymentService(
        private val invoiceRepository: InvoiceRepository,
        private val paymentRepository: PaymentRepository,
        private val commandBus: CommandBus) {

    private val logger = KotlinLogging.logger {}

    suspend fun execute(command: CreatePaymentCommand): Either<Throwable, Event> = Either.catch {
        logger.info { "Create payment for invoice ${command.invoiceId}" }
        Payment.create(command)
                .also { payment -> paymentRepository.save(payment) }
                .let { PaymentCreatedEvent(it.transactionId, it.invoiceId, it.billingId, it.status) }
    }

    suspend fun execute(command: CompletePaymentCommand): Either<Throwable, Event> = Either.catch {
        logger.info { "Complete payment for invoice ${command.invoiceId}" }

        paymentRepository.load(command.transactionId)
                ?.complete()
                ?.also { payment -> paymentRepository.update(payment) }
                ?.let {
                    PaymentCompletedEvent(it.transactionId, it.invoiceId, it.billingId, it.status)
                } ?: throw PaymentNotFoundException("Payment ${command.transactionId} not found")
    }

    suspend fun execute(command: CancelPaymentCommand): Either<Throwable, Event> = Either.catch {
        logger.info { "Cancel payment for invoice ${command.invoiceId}" }

        paymentRepository.load(command.transactionId)
                ?.cancel(command.cancellationDescription)
                ?.also { payment -> paymentRepository.update(payment) }
                ?.let {
                    PaymentCanceledEvent(it.transactionId,
                            it.invoiceId,
                            it.cancellationReason,
                            it.billingId,
                            it.status)
                } ?: throw PaymentNotFoundException("Payment ${command.transactionId} not found")
    }

    suspend fun payInvoice(invoiceId: Int) {

        val invoice = invoiceRepository.loadBlocking(invoiceId)
                ?.takeIf { it.status == InvoiceStatus.PENDING }
                ?: throw throw InvoiceNotFoundException(invoiceId)

        commandBus.sendAwait(CreatePaymentCommand(invoice.id))
    }

    fun fetchAll(): List<Payment> = paymentRepository.fetchAll()
}