package io.pleo.antaeus.context.payment

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.pleo.antaeus.core.messagebus.EventBus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class PaymentCommandHandlerTest {
    companion object {
        val TRANSACTION_ID: UUID = UUID.randomUUID()
        const val INVOICE_ID = 1234
        val PROCESS_ID: UUID = UUID.randomUUID()
    }

    private val eventBus = mockk<EventBus>(relaxed = true)
    private val repository = mockk<PaymentRepository>(relaxed = true)
    private val commandHandler = PaymentCommandHandler(repository, eventBus)


    @Test
    fun `should create a new invoice payment transaction`() {
        val slot = slot<Payment>()
        val payment = Payment(TRANSACTION_ID, INVOICE_ID, PaymentStatus.STARTED, PaymentCancellationReason.NONE, PROCESS_ID)
        every {
            repository.save(payment = capture(slot))
        } returns payment
        //when
        commandHandler.handle(RequestPaymentCommand(INVOICE_ID, PROCESS_ID))
        //then

        verify {
            eventBus.publish(PaymentRequestedEvent(payment.transactionId, payment.invoiceId, payment.billingProcessId))
        }
        assertNotNull(slot.captured.transactionId)
        assertEquals(slot.captured.invoiceId, INVOICE_ID)
        assertEquals(slot.captured.billingProcessId, PROCESS_ID)
        assertEquals(slot.captured.paymentCancellationReason, PaymentCancellationReason.NONE)
        assertEquals(slot.captured.status, PaymentStatus.STARTED)
    }

    @Test
    fun `should fail when trying to complete payment already completed`() {

        //given
        every { repository.fetch(TRANSACTION_ID) } returns
                Payment(TRANSACTION_ID,
                        INVOICE_ID,
                        PaymentStatus.COMPLETED,
                        PaymentCancellationReason.NONE,
                        PROCESS_ID)

        assertThrows<InvalidPaymentStatusException> {
            commandHandler.handle(CompletePaymentCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }

    @Test
    fun `should fail when trying to complete payment already canceled`() {

        //given
        every { repository.fetch(TRANSACTION_ID) } returns
                Payment(TRANSACTION_ID,
                        INVOICE_ID,
                        PaymentStatus.CANCELED,
                        PaymentCancellationReason.NONE,
                        PROCESS_ID)

        assertThrows<InvalidPaymentStatusException> {
            commandHandler.handle(CompletePaymentCommand(TRANSACTION_ID, INVOICE_ID))
        }
    }


    @Test
    fun `should complete invoice payment transaction`() {
        //given
        every { repository.fetch(TRANSACTION_ID) } returns
                Payment(TRANSACTION_ID,
                        INVOICE_ID,
                        PaymentStatus.STARTED,
                        PaymentCancellationReason.NONE,
                        PROCESS_ID)
        //when
        commandHandler.handle(CompletePaymentCommand(TRANSACTION_ID, INVOICE_ID))
        //then
        verify {
            repository.update(Payment(TRANSACTION_ID, INVOICE_ID,
                    PaymentStatus.COMPLETED,
                    PaymentCancellationReason.NONE, PROCESS_ID))
            eventBus.publish(PaymentCompletedEvent(TRANSACTION_ID, INVOICE_ID, PROCESS_ID))
        }
    }


    @Test
    fun `should fail when trying to cancel payment already completed`() {

        //given
        every { repository.fetch(TRANSACTION_ID) } returns
                Payment(TRANSACTION_ID,
                        INVOICE_ID,
                        PaymentStatus.COMPLETED,
                        PaymentCancellationReason.NONE,
                        PROCESS_ID)

        assertThrows<InvalidPaymentStatusException> {
            commandHandler.handle(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.UNKNOWN))
        }
    }

    @Test
    fun `should fail when trying to cancel payment already canceled`() {

        //given
        every { repository.fetch(TRANSACTION_ID) } returns
                Payment(TRANSACTION_ID,
                        INVOICE_ID,
                        PaymentStatus.CANCELED,
                        PaymentCancellationReason.NONE,
                        PROCESS_ID)

        assertThrows<InvalidPaymentStatusException> {
            commandHandler.handle(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.UNKNOWN))
        }
    }

    @Test
    fun `should cancel invoice payment transaction`() {
        //given
        every { repository.fetch(TRANSACTION_ID) } returns Payment(TRANSACTION_ID,
                INVOICE_ID, PaymentStatus.STARTED, PaymentCancellationReason.NONE, PROCESS_ID)
        //when
        commandHandler.handle(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.CUSTOMER_NOT_FOUND))
        //then
        verify {
            repository.update(Payment(TRANSACTION_ID, INVOICE_ID,
                    PaymentStatus.CANCELED,
                    PaymentCancellationReason.CUSTOMER_NOT_FOUND, PROCESS_ID))
            eventBus.publish(PaymentCanceledEvent(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.CUSTOMER_NOT_FOUND, PROCESS_ID))
        }
    }
}