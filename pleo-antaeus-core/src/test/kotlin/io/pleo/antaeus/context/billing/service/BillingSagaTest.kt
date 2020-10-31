package io.pleo.antaeus.context.billing.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.billing.BillingSaga
import io.pleo.antaeus.context.billing.CompleteBillingCommand
import io.pleo.antaeus.context.billing.BillingRequestedEvent
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.context.payment.RequestPaymentCommand
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class BillingSagaTest {

    companion object {
        val PROCESS_ID: UUID = UUID.fromString("a89d9213-fb69-4c05-b1c3-cf078385ca98")
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val invoiceService = mockk<InvoiceService>()
    private val commandBus = mockk<CommandBus>(relaxed = true)
    private val billingSagaService = BillingSaga(invoiceService, commandBus)

    @Test
    fun `should complete billing bath process when no pending invoices found`() {
        //given
        every { invoiceService.fetchAllPending() } returns emptyList()
        //when
        billingSagaService.handle(BillingRequestedEvent(PROCESS_ID))

        //then
        verify { commandBus.send(CompleteBillingCommand(PROCESS_ID)) }
        verify(exactly = 0) { commandBus.send(ofType(RequestPaymentCommand::class)) }
    }

    @Test
    fun `should request invoice payment for evert pending invoice`() {
        //given
        val invoice1 = Invoice(1, 2, TEN_EURO, InvoiceStatus.PENDING)
        val invoice2 = Invoice(2, 3, TEN_EURO, InvoiceStatus.PENDING)
        every { invoiceService.fetchAllPending() } returns listOf(invoice1, invoice2)

        //when
        billingSagaService.handle(BillingRequestedEvent(PROCESS_ID))

        //then
//        val slot = slot<RequestPaymentCommand>()
        verify(exactly = 0) { commandBus.send(ofType(CompleteBillingCommand::class)) }
        verify(exactly = 2) {
            commandBus.send(ofType(RequestPaymentCommand::class))
        }

    }
}