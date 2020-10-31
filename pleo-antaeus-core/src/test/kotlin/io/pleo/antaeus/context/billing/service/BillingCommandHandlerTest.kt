package io.pleo.antaeus.context.billing.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.core.messagebus.EventBus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class BillingCommandHandlerTest {

    companion object {
        val PROCESS_ID: UUID = UUID.fromString("a89d9213-fb69-4c05-b1c3-cf078385ca98")
    }

    private val repository = mockk<BillingRepository>(relaxed = true)
    private val eventBus = mockk<EventBus>(relaxed = true)

    private val billingService = BillingCommandHandler(
            repository = repository,
            eventBus = eventBus
    )

    @Test
    fun `should fail when transaction not created`() {
        //given
        every { repository.save(any()) } returns null

        //expect
        assertThrows<InvalidBillingTransactionException> {
            billingService.handle(StartBillingCommand(PROCESS_ID))
        }
    }

    @Test
    fun `should start process when transaction created`() {
        //given
        val billing = Billing(PROCESS_ID, BillingStatus.STARTED, emptyList())
        every { repository.save(Billing(PROCESS_ID, BillingStatus.STARTED, emptyList())) } returns billing

        //when
        billingService.handle(StartBillingCommand(PROCESS_ID))

        //then
        verify { eventBus.publish(BillingRequestedEvent(billing.processId)) }
    }

    @Test
    fun `should not complete process when have any started yet`() {

        //given
        every { repository.load(PROCESS_ID) } returns Billing(PROCESS_ID, BillingStatus.STARTED,
                listOf(
                        Payment(UUID.randomUUID(),
                                1234,
                                PaymentStatus.COMPLETED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID),
                        Payment(UUID.randomUUID(),
                                1235,
                                PaymentStatus.COMPLETED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID),
                        Payment(UUID.randomUUID(),
                                1236,
                                PaymentStatus.STARTED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID)
                )
        )

        //when
        billingService.handle(CompleteBillingCommand(PROCESS_ID))

        //then
        verify(exactly = 0) {
            repository.update(ofType(Billing::class))
            eventBus.publish(ofType(BillingCompletedEvent::class))
        }
    }

    @Test
    fun `should complete process when have no any invoice payment transaction`() {

        //given
        val billing = Billing(PROCESS_ID, BillingStatus.STARTED, emptyList())
        every { repository.load(PROCESS_ID) } returns billing

        //when
        billingService.handle(CompleteBillingCommand(PROCESS_ID))

        //then
        verify(exactly = 1) {
            repository.update(billing.copy(status = BillingStatus.COMPLETED))
            eventBus.publish(BillingCompletedEvent(PROCESS_ID))
        }
    }

    @Test
    fun `should complete process when have all invoice payment transaction finished`() {

        //given
        val billing = Billing(PROCESS_ID, BillingStatus.STARTED,
                listOf(
                        Payment(UUID.randomUUID(),
                                1234,
                                PaymentStatus.COMPLETED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID),
                        Payment(UUID.randomUUID(),
                                1235,
                                PaymentStatus.COMPLETED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID),
                        Payment(UUID.randomUUID(),
                                1236,
                                PaymentStatus.CANCELED,
                                PaymentCancellationReason.NONE,
                                PROCESS_ID)
                )
        )
        every { repository.load(PROCESS_ID) } returns billing


        //when
        billingService.handle(CompleteBillingCommand(PROCESS_ID))

        //then
        verify(exactly = 1) {
            repository.update(billing.copy(status = BillingStatus.COMPLETED))
            eventBus.publish(BillingCompletedEvent(PROCESS_ID))
        }
    }
}