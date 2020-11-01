package io.pleo.antaeus.context.invoice

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class InvoiceCommandHandlerTest {
    companion object {
        val TRANSACTION_ID: UUID = UUID.randomUUID()
        const val INVOICE_ID = 1234
        const val CUSTOMER_ID = 1234
        val TEN_EUROS = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val eventBus = mockk<EventBus>(relaxed = true)
    private val repository = mockk<InvoiceRepository>(relaxed = true)
    private val paymentProvider = mockk<PaymentProvider>()
    private val commandHandler = InvoiceCommandHandler(repository, paymentProvider, eventBus)

    @Test
    fun `should fail when invoice is paid`() {
        //given
        val existingInvoice = Invoice(INVOICE_ID, CUSTOMER_ID, TEN_EUROS, InvoiceStatus.PAID,1)
        every { repository.load(INVOICE_ID) } returns existingInvoice


        assertThrows<InvalidInvoiceStatusException> {
            commandHandler.handle(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }

        verify (exactly = 0) { paymentProvider.charge(any()) }
    }

    @Test
    fun `should fail when payment provider fails`() {
        //given
        val existingInvoice = Invoice(INVOICE_ID, CUSTOMER_ID, TEN_EUROS, InvoiceStatus.PENDING,1)
        every { repository.load(INVOICE_ID) } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } returns false

        assertThrows<AccountBalanceException> {
            commandHandler.handle(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

    @Test
    fun `should fail when payment provider fails due to customer not found`() {
        //given
        val existingInvoice = Invoice(INVOICE_ID, CUSTOMER_ID, TEN_EUROS, InvoiceStatus.PENDING,1)
        every { repository.load(INVOICE_ID) } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } throws CustomerNotFoundException(1111)

        assertThrows<CustomerNotFoundException> {
            commandHandler.handle(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

    @Test
    fun `should fail when payment provider fails due to currency mismatch`() {
        //given
        val existingInvoice = Invoice(INVOICE_ID, CUSTOMER_ID, TEN_EUROS, InvoiceStatus.PENDING,1)
        every { repository.load(INVOICE_ID) } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } throws CurrencyMismatchException(1111, INVOICE_ID)

        assertThrows<CurrencyMismatchException> {
            commandHandler.handle(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

    @Test
    fun `should fail when payment provider fails due to network error`() {
        //given
        val existingInvoice = Invoice(INVOICE_ID, CUSTOMER_ID, TEN_EUROS, InvoiceStatus.PENDING,1)
        every { repository.load(INVOICE_ID) } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } throws NetworkException()

        assertThrows<NetworkException> {
            commandHandler.handle(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

}