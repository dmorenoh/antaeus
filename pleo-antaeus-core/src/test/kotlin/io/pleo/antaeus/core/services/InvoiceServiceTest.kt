package io.pleo.antaeus.core.services

import io.mockk.mockk
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.messagebus.EventBus

class InvoiceServiceTest {

    companion object {
        const val TRANSACTION_ID = "transactionId"
        const val INVOICE_ID = 1234
    }

    private val repository = mockk<InvoiceRepository>(relaxed = true)

    private val paymentProvider = mockk<PaymentProvider>()


    private val eventBus = mockk<EventBus>(relaxed = true)

    private val invoiceService = InvoiceService(repository = repository)


//    @Test
//    fun `will throw if invoice is not found`() {
//        assertThrows<InvoiceNotFoundException> {
//            invoiceService.fetch(404)
//        }
//    }
//
//    @Test
//    fun `should fail when trying to paid an paid invoice`() {
//        every { dal.fetchInvoice(INVOICE_ID) } returns Invoice(INVOICE_ID,
//                1234,
//                Money(BigDecimal.TEN, Currency.EUR),
//                InvoiceStatus.PAID)
//
//        assertThrows<InvalidInvoiceStatusException> {
//            invoiceService.on(command = PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
//        }
//    }
//
//    @Test
//    fun `should fail when not charged`() {
//        val pendingInvoice = Invoice(INVOICE_ID,
//                1234,
//                Money(BigDecimal.TEN, Currency.EUR),
//                InvoiceStatus.PENDING)
//        every { dal.fetchInvoice(INVOICE_ID) } returns pendingInvoice
//        every { paymentProvider.charge(pendingInvoice) } returns false
//
//        assertThrows<AccountBalanceException> {
//            invoiceService.on(command = PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID))
//        }
//
//        verify (exactly = 0) {
//            dal.update(ofType(Invoice::class))
//            eventBus.publish(ofType(InvoicePaidEvent::class))
//        }
//



}
