package io.pleo.antaeus.context.payment

import io.kotest.assertions.arrow.either.shouldBeRight
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class PaymentServiceTest {

    companion object {
        const val INVOICE_ID = 1234
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
        val CUSTOMER = Customer(1, Currency.EUR)
    }

    private val invoiceRepository: InvoiceRepository = mockk(relaxed = true)
    private val paymentRepository: PaymentRepository = mockk(relaxed = true)
    private val commandBus = mockk<CommandBus>(relaxed = true)
    private val paymentService = PaymentService(invoiceRepository, paymentRepository, commandBus)

    @Test
    fun `should create payment when requested`() {
        val result = runBlocking {
            paymentService.execute(CreatePaymentCommand(1))
        }

        val paymentSlot = slot<Payment>()

        verify { paymentRepository.save(payment = capture(paymentSlot)) }
        val payment = paymentSlot.captured

        assert(payment.status == PaymentStatus.STARTED)

        result.shouldBeRight { event ->
            assert(event as PaymentCreatedEvent ==
                    PaymentCreatedEvent(payment.transactionId,
                            payment.invoiceId,
                            null,
                            payment.status))
        }
    }

    @Test
    fun `should fail when no invoice found`() {
        every { invoiceRepository.load(INVOICE_ID) } returns null

        assertThrows<InvoiceNotFoundException> {
            runBlocking {
                paymentService.payInvoice(INVOICE_ID)
            }
        }
        verify(exactly = 0) { runBlocking { commandBus.sendAwait(any()) } }
    }

    @Test
    fun `should fail when invoice found and no pending`() {

        val aPaidInvoice = Invoice(INVOICE_ID, CUSTOMER, TEN_EURO, InvoiceStatus.PAID, 1)

        every { invoiceRepository.load(INVOICE_ID) } returns aPaidInvoice

        assertThrows<InvoiceNotFoundException> {
            runBlocking {
                paymentService.payInvoice(INVOICE_ID)
            }
        }
        verify(exactly = 0) { runBlocking { commandBus.sendAwait(any()) } }
    }

    @Test
    fun `should request create payment when invoice pending found`() {
        val aPendingInvoice = Invoice(INVOICE_ID, CUSTOMER, TEN_EURO, InvoiceStatus.PENDING, 1)

        every { invoiceRepository.load(INVOICE_ID) } returns aPendingInvoice

        runBlocking { paymentService.payInvoice(INVOICE_ID) }

        verify { runBlocking { commandBus.sendAwait(CreatePaymentCommand(INVOICE_ID)) } }
    }
}