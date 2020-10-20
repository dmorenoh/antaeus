package io.pleo.antaeus.context.billing.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.billing.command.CompleteBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.command.StartBillingBatchProcessCommand
import io.pleo.antaeus.context.billing.dal.BillingDal
import io.pleo.antaeus.context.billing.event.BillingBatchProcessStartedEvent
import io.pleo.antaeus.context.billing.exceptions.InvalidBillingTransactionException
import io.pleo.antaeus.context.invoice.service.InvoiceService
import io.pleo.antaeus.context.payment.InvoicePaymentService
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.messagebus.EventBus
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class BillingServiceTest {

    companion object {
        const val PROCESS_ID = 1234
    }

    private val invoicePaymentService = mockk<InvoicePaymentService>()
    private val dal = mockk<BillingDal>(relaxed = true)
    private val eventBus = mockk<EventBus>(relaxed = true)

    private val billingService = BillingService(
            invoicePaymentService = invoicePaymentService,
            billingDal = dal,
            eventBus = eventBus
    )

    @Test
    fun `should fail when transaction not created`() {
        //given
        every { dal.save(BillingBatchProcess(PROCESS_ID)) } returns null

        //expect
        assertThrows<InvalidBillingTransactionException> {
            billingService.on(command = StartBillingBatchProcessCommand(processId = PROCESS_ID))

        }
    }

    @Test
    fun `should start process when transaction created`() {
        //given
        every { dal.save(BillingBatchProcess(1111)) } returns BillingBatchProcess(1111)

        //when
        billingService.on(command = StartBillingBatchProcessCommand(1111))

        //then
        verify { eventBus.publish(BillingBatchProcessStartedEvent(1111)) }
    }

    @Test
    fun `should not complete process when have any started yet`() {

        //given
        every { dal.fetch(PROCESS_ID) } returns BillingBatchProcess(PROCESS_ID)
        every { invoicePaymentService.fetchByBillingProcessId(PROCESS_ID) } returns listOf(
                InvoicePaymentTransaction("transaction1",
                        1234,
                        PaymentStatus.COMPLETED,
                        PaymentFailureReason.NONE,
                        PROCESS_ID),
                InvoicePaymentTransaction("transaction2",
                        1235,
                        PaymentStatus.COMPLETED,
                        PaymentFailureReason.NONE,
                        PROCESS_ID),
                InvoicePaymentTransaction("transaction3",
                        1236,
                        PaymentStatus.STARTED,
                        PaymentFailureReason.NONE,
                        PROCESS_ID))

        //when
        billingService.on(CompleteBillingBatchProcessCommand(PROCESS_ID))

        //then
        verify(exactly = 0) { dal.update(ofType(BillingBatchProcess::class)) }
    }

    @Test
    fun `should complete process when have no any invoice payment transaction`() {

        //given
        every { dal.fetch(PROCESS_ID) } returns BillingBatchProcess(PROCESS_ID)
        every { invoicePaymentService.fetchByBillingProcessId(PROCESS_ID) } returns emptyList()

        //when
        billingService.on(CompleteBillingBatchProcessCommand(PROCESS_ID))

        //then
        verify(exactly = 1) { dal.update(BillingBatchProcess(PROCESS_ID,BillingStatus.COMPLETED)) }
    }
}