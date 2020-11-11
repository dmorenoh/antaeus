package io.pleo.antaeus.context.payment

import io.mockk.mockk
import io.pleo.antaeus.core.messagebus.CommandBus
import java.util.*

class PaymentSagaOldTest {

    companion object {
        val TRANSACTION_ID:UUID = UUID.randomUUID()
        const val INVOICE_ID = 1111
    }

    private val commandBus = mockk<CommandBus>(relaxed = true)
//    private val invoicePaymentSagaService = PaymentSagaOld(commandBus)

//    @Test
//    fun `should cancel payment when payment invoice fail as Account Balance Exception`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(AccountBalanceException("test"))
////        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
////        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.ACCOUNT_BALANCE_ISSUE)) }
//    }
//
//    @Test
//    fun `should cancel payment when payment invoice fail as Invalid Invoice Status`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(InvalidInvoiceStatusException("test"))
////        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
////        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.INVALID_INVOICE_STATUS)) }
//    }
//
//    @Test
//    fun `should cancel payment when payment invoice fail as Customer Not Found`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(CustomerNotFoundException(1))
////        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
//        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.CUSTOMER_NOT_FOUND)) }
//    }
//
//    @Test
//    fun `should cancel payment when payment invoice fail as Currency Mismatch`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(CurrencyMismatchException(INVOICE_ID, 1))
////        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
////        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.CURRENCY_MISMATCH)) }
//    }
//
//    @Test
//    fun `should cancel payment when payment invoice fail as Network exception`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(NetworkException())
//        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
//        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.NETWORK_FAILED)) }
//    }
//
//    @Test
//    fun `should cancel payment when payment invoice fail as general exception`() {
//        //given
//        val failedFuture = CompletableFuture<Void>()
//        failedFuture.completeExceptionally(RuntimeException())
//        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns failedFuture
//
//        //when
//        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify { commandBus.send(CancelPaymentCommand(TRANSACTION_ID, INVOICE_ID, PaymentCancellationReason.UNKNOWN)) }
//    }
//
//    @Test
//    fun `should do not cancel when payment invoice does not fail`() {
//        //given
//        val completeFuture = CompletableFuture<Void>()
//        completeFuture.complete(null)
//        every { commandBus.send(PayInvoiceCommand(TRANSACTION_ID, INVOICE_ID)) } returns completeFuture
//
//        //when
//        invoicePaymentSagaService.on(PaymentCreatedEvent(TRANSACTION_ID, INVOICE_ID))
//
//        //then
//        verify(exactly = 0) { commandBus.send(ofType(CancelPaymentCommand::class)) }
//    }
}