package io.pleo.antaeus.context.billing

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BillingServiceTest {
    companion object {
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val commandBus = mockk<CommandBus>(relaxed = true)
    private val invoiceService = mockk<InvoiceService>(relaxed = true)
    private var billingService = BillingService(invoiceService = invoiceService, commandBus = commandBus)


    @Test
    fun `should do nothing when no pending invoices to process`() {
        every { invoiceService.fetchAllPending() } returns emptyList()
        billingService.startProcess()
        verify(exactly = 0) { commandBus.send(any()) }
    }

    @Test
    fun `should process when pending invoices found`() {
        val customer = Customer(1, Currency.EUR)
        every { invoiceService.fetchAllPending() } returns listOf(
                Invoice(1, customer, TEN_EURO, InvoiceStatus.PENDING, 1),
                Invoice(2, customer, TEN_EURO, InvoiceStatus.PENDING, 1),
                Invoice(3, customer, TEN_EURO, InvoiceStatus.PENDING, 1)
        )
        billingService.startProcess()
        verify(exactly = 1) { commandBus.send(StartBillingCommand(listOf(1, 2, 3))) }
    }
}