package io.pleo.antaeus.context.billing.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.context.billing.command.CompleteBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.event.BillingBatchProcessStartedEvent
import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.context.payment.RequestInvoicePaymentCommand
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingSagaServiceTest {

    companion object {
        const val PROCESS_ID = 12314
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val invoiceService = mockk<InvoiceService>()
    private val commandBus = mockk<CommandBus>(relaxed = true)
    private val billingSagaService = BillingSagaService(invoiceService, commandBus)

    @Test
    fun `should complete billing bath process when no pending invoices found`() {
        //given
        every { invoiceService.fetchAllPending() } returns emptyList()
        //when
        billingSagaService.handle(BillingBatchProcessStartedEvent(PROCESS_ID))

        //then
        verify { commandBus.send(CompleteBillingBatchProcessCommand(PROCESS_ID)) }
        verify(exactly = 0) { commandBus.send(ofType(RequestInvoicePaymentCommand::class)) }
    }

    @Test
    fun `should request invoice payment for evert pending invoice`() {
        //given
        val invoice1 = Invoice(1, 2, TEN_EURO, InvoiceStatus.PENDING)
        val invoice2 = Invoice(2, 3, TEN_EURO, InvoiceStatus.PENDING)
        every { invoiceService.fetchAllPending() } returns listOf(invoice1, invoice2)

        //when
        billingSagaService.handle(BillingBatchProcessStartedEvent(PROCESS_ID))

        //then
        val slot = slot<RequestInvoicePaymentCommand>()
        verify(exactly = 0) { commandBus.send(ofType(CompleteBillingBatchProcessCommand::class)) }
        verify(exactly = 2) {
            commandBus.send(ofType(RequestInvoicePaymentCommand::class))
        }

    }
}