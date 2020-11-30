package io.pleo.antaeus.context.invoice

import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.payment.ChargeInvoiceCommand
import io.pleo.antaeus.context.payment.RevertPaymentCommand
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class InvoiceServiceTest {
    companion object {
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val invoiceRepository: InvoiceRepository = mockk(relaxed = true)
    private val paymentProvider: PaymentProvider = mockk(relaxed = true)
    private val invoiceService = InvoiceService(invoiceRepository, paymentProvider)

    @Test
    fun `should fail to pay invoice when invoice does not exist`() {
        every { runBlocking { invoiceRepository.load(1) } } returns null

        val result = runBlocking {
            invoiceService.execute(PayInvoiceCommand(UUID.randomUUID(), 1))
        }

        result.shouldBeLeft { error -> assert(error is InvoiceNotFoundException) }
    }

    @Test
    fun `should fail to pay invoice when status is PAID`() {
        val customer = Customer(1, Currency.EUR)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PAID, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        val result = runBlocking {
            invoiceService.execute(PayInvoiceCommand(UUID.randomUUID(), 1))
        }
        result.shouldBeLeft { error -> assert(error is InvalidInvoiceStatusException) }
    }

    @Test
    fun `should fail to pay invoice when currency mismatch`() {

        val customer = Customer(1, Currency.USD)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PENDING, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        val result = runBlocking {
            invoiceService.execute(PayInvoiceCommand(UUID.randomUUID(), 1))
        }
        result.shouldBeLeft { error -> assert(error is CurrencyMismatchException) }
    }

    @Test
    fun `should pay invoice when valid pending invoice`() {
        val transactionId = UUID.randomUUID()
        val customer = Customer(1, Currency.EUR)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PENDING, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        val result = runBlocking {
            invoiceService.execute(PayInvoiceCommand(transactionId, 1))
        }
        result.shouldBeRight { event ->
            assert(event as InvoicePaidEvent == InvoicePaidEvent(transactionId, 1, InvoiceStatus.PAID))
        }
    }

    @Test
    fun `should fail charging invoice when payment provider fails`() {
        val transactionId = UUID.randomUUID()
        val customer = Customer(1, Currency.USD)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PAID, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } throws NetworkException()
        val result = runBlocking {
            invoiceService.execute(ChargeInvoiceCommand(transactionId, 1))
        }
        result.shouldBeLeft { error -> assert(error is NetworkException) }
    }

    @Test
    fun `should fail charging invoice when payment provider returns false`() {
        val transactionId = UUID.randomUUID()
        val customer = Customer(1, Currency.USD)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PAID, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } returns false
        val result = runBlocking {
            invoiceService.execute(ChargeInvoiceCommand(transactionId, 1))
        }
        result.shouldBeLeft { error -> assert(error is AccountBalanceException) }
    }

    @Test
    fun `should charge invoice when payment provider returns true`() {
        val transactionId = UUID.randomUUID()
        val customer = Customer(1, Currency.USD)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PAID, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        every { paymentProvider.charge(existingInvoice) } returns true
        val result = runBlocking {
            invoiceService.execute(ChargeInvoiceCommand(transactionId, 1))
        }
        result.shouldBeRight { event ->
            assert(event as InvoiceChargedEvent == InvoiceChargedEvent(transactionId, 1))
        }
    }

    @Test
    fun `should revert payment when requested for an existing invoice`() {
        val transactionId = UUID.randomUUID()
        val anyReason = "any reason"
        val customer = Customer(1, Currency.USD)
        val existingInvoice = Invoice(1, customer, TEN_EURO, InvoiceStatus.PAID, 1)
        every { runBlocking { invoiceRepository.load(1) } } returns existingInvoice
        val result = runBlocking {
            invoiceService.execute(RevertPaymentCommand(transactionId, 1, anyReason))
        }
        result.shouldBeRight { event ->
            assert(event as PaymentRevertedEvent == PaymentRevertedEvent(transactionId, 1, InvoiceStatus.PENDING, anyReason))
        }
    }

}