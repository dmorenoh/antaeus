package io.pleo.antaeus.context.billing

import arrow.core.Either
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.core.messagebus.CommandBus
import mu.KotlinLogging


class BillingService(
        private val billingRepository: BillingRepository,
        private val invoiceService: InvoiceService,
        private val commandBus: CommandBus) {


    private val logger = KotlinLogging.logger {}

    suspend fun execute(command: StartBillingCommand): Either<Throwable, Event> = Either.catch {
        Billing.create(command = command)
                .also { billingRepository.save(billing = it) }
                .let { BillingStartedEvent(it.processId, it.invoicesId()) }
    }

    suspend fun execute(command: CloseBillingInvoiceCommand): Either<Throwable, Event> = Either.catch {

        val billing = billingRepository.load(command.billingId)
                ?: throw BillingNotFoundException("Billing ${command.billingId}")

        billing.closeInvoice(command.invoicesId)
                .let {
                    if (it.isComplete())
                        BillingCompletedEvent(it.processId)
                    else {
                        BillingInvoiceCompletedEvent(it.processId, command.invoicesId)
                    }
                }
    }

    fun startProcess() = invoiceService.fetchAllPending()
            .map(Invoice::id)
            .takeIf { it.isNotEmpty() }
            ?.let { pendingInvoiceIds -> commandBus.send(StartBillingCommand(pendingInvoiceIds)) }
            ?: logger.info { "Nothing to process" }

    fun fetchAll() = billingRepository.fetchAll()
}