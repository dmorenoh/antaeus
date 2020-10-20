package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.invoice.command.PayInvoiceCommand
import io.pleo.antaeus.context.invoice.dal.InvoiceDal
import io.pleo.antaeus.context.invoice.event.InvoicePaidEvent
import io.pleo.antaeus.context.invoice.exception.AccountBalanceException
import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.core.exceptions.InvalidInvoiceStatusException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {

    companion object {
        const val TRANSACTION_ID = "transactionId"
        const val INVOICE_ID = 1234
    }

    private val dal = mockk<InvoiceDal>(relaxed = true)

    private val paymentProvider = mockk<PaymentProvider>()


    private val eventBus = mockk<EventBus>(relaxed = true)

    private val invoiceService = InvoiceService(dal = dal, paymentProvider = paymentProvider, eventBus = eventBus)


//    @Test
//    fun `will throw if invoice is not found`() {
//        assertThrows<InvoiceNotFoundException> {
//            invoiceService.fetch(404)
//        }
//    }

    @Test
    fun `should fail when trying to paid an paid invoice`() {
        every { dal.fetchInvoice(INVOICE_ID) } returns Invoice(INVOICE_ID,
                1234,
                Money(BigDecimal.TEN, Currency.EUR),
                InvoiceStatus.PAID)

        assertThrows<InvalidInvoiceStatusException> {
            invoiceService.on(command = PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

    @Test
    fun `should fail when not charged`() {
        val pendingInvoice = Invoice(INVOICE_ID,
                1234,
                Money(BigDecimal.TEN, Currency.EUR),
                InvoiceStatus.PENDING)
        every { dal.fetchInvoice(INVOICE_ID) } returns pendingInvoice
        every { paymentProvider.charge(pendingInvoice) } returns false

        assertThrows<AccountBalanceException> {
            invoiceService.on(command = PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
        }

        verify (exactly = 0) {
            dal.update(ofType(Invoice::class))
            eventBus.publish(ofType(InvoicePaidEvent::class))
        }


    }

}
