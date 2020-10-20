package io.pleo.antaeus.context.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.InvoicePaymentTransaction
import io.pleo.antaeus.models.PaymentFailureReason
import io.pleo.antaeus.models.PaymentStatus
import org.junit.jupiter.api.Test

class InvoicePaymentServiceTest {
    companion object {
        const val TRANSACTION_ID = "transactionID"
        const val INVOICE_ID = 1234
        const val PROCESS_ID = 1111
    }

    private val eventBus = mockk<EventBus>(relaxed = true)
    private val dal = mockk<InvoicePaymentDal>(relaxed = true)
    private val invoicePaymentService = InvoicePaymentService(dal, eventBus)

    @Test
    fun `should create a new invoice payment transaction`() {
        //when
        invoicePaymentService.on(RequestInvoicePaymentCommand(TRANSACTION_ID, INVOICE_ID, PROCESS_ID))
        //then
        verify {
            dal.save(InvoicePaymentTransaction(TRANSACTION_ID, INVOICE_ID,
                    PaymentStatus.STARTED,
                    PaymentFailureReason.NONE, PROCESS_ID))
            eventBus.publish(InvoicePaymentRequestedEvent(TRANSACTION_ID, INVOICE_ID, PROCESS_ID))
        }
    }

    @Test
    fun `should complete invoice payment transaction`() {
        //given
        every { dal.fetch(TRANSACTION_ID) } returns InvoicePaymentTransaction(TRANSACTION_ID,
                INVOICE_ID, PaymentStatus.STARTED, PaymentFailureReason.NONE, PROCESS_ID)
        //when
        invoicePaymentService.on(CompleteInvoicePaymentCommand(TRANSACTION_ID, INVOICE_ID))
        //then
        verify {
            dal.update(InvoicePaymentTransaction(TRANSACTION_ID, INVOICE_ID,
                    PaymentStatus.COMPLETED,
                    PaymentFailureReason.NONE, PROCESS_ID))
            eventBus.publish(InvoicePaymentCompletedEvent(TRANSACTION_ID, INVOICE_ID, PROCESS_ID))
        }
    }

    @Test
    fun `should cancel invoice payment transaction`() {
        //given
        every { dal.fetch(TRANSACTION_ID) } returns InvoicePaymentTransaction(TRANSACTION_ID,
                INVOICE_ID, PaymentStatus.STARTED, PaymentFailureReason.NONE, PROCESS_ID)
        //when
        invoicePaymentService.on(CancelInvoicePaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentFailureReason.CUSTOMER_NOT_FOUND))
        //then
        verify {
            dal.update(InvoicePaymentTransaction(TRANSACTION_ID, INVOICE_ID,
                    PaymentStatus.CANCELED,
                    PaymentFailureReason.CUSTOMER_NOT_FOUND, PROCESS_ID))
            eventBus.publish(InvoicePaymentCanceledEvent(TRANSACTION_ID, INVOICE_ID, PaymentFailureReason.CUSTOMER_NOT_FOUND, PROCESS_ID))
        }
    }
}