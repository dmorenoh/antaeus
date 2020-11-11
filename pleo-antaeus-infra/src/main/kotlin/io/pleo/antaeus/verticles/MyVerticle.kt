package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.invoice.InvoiceCommandHandlerOld
import io.pleo.antaeus.context.payment.PaymentCommandHandler
import io.pleo.antaeus.context.payment.PaymentSagaOld
import io.vertx.kotlin.coroutines.CoroutineVerticle
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class MyVerticle(private val invoiceCommandHandlerOld: InvoiceCommandHandlerOld,
                 private val paymentCommandHandler: PaymentCommandHandler,
                 private val paymentSagaOld: PaymentSagaOld
) : CoroutineVerticle() {
    override suspend fun start() {

//        vertx.eventBus().registerDefaultCodec(PaymentRequestedEvent::class.java,
//                GenericCodec(PaymentRequestedEvent::class.java))
//
//        vertx.eventBus().registerDefaultCodec(PaymentCompletedEvent::class.java,
//                GenericCodec(PaymentCompletedEvent::class.java))
//
//        vertx.eventBus().registerDefaultCodec(PaymentCanceledEvent::class.java,
//                GenericCodec(PaymentCanceledEvent::class.java))
//
//        vertx.eventBus().registerDefaultCodec(PayInvoiceCommand::class.java,
//                GenericCodec(PayInvoiceCommand::class.java))
//
//        vertx.eventBus().registerDefaultCodec(RequestPaymentCommand::class.java,
//                GenericCodec(RequestPaymentCommand::class.java))
//
//        vertx.eventBus().registerDefaultCodec(CompletePaymentCommand::class.java,
//                GenericCodec(CompletePaymentCommand::class.java))
//
//        vertx.eventBus().registerDefaultCodec(CancelPaymentCommand::class.java,
//                GenericCodec(CancelPaymentCommand::class.java))
//
//        vertx.eventBus().registerDefaultCodec(InvoicePaidEvent::class.java,
//                GenericCodec(InvoicePaidEvent::class.java))
//
//        vertx.eventBus().registerDefaultCodec(PaymentFailedEvent::class.java,
//                GenericCodec(PaymentFailedEvent::class.java))
//
//
//        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") {
//            launch {
//                logger.info { "Launched handler PayInvoiceCommand ${Thread.currentThread().name}" }
//                awaitBlocking {
//                    logger.info { "Launched handler blocking PayInvoiceCommand ${Thread.currentThread().name}" }
//                    invoiceCommandHandler.handle(it.body())
//                }
//                logger.info { "Leaving handler PayInvoiceCommand Launched ${Thread.currentThread().name}" }
//            }
//        }
//
//        vertx.eventBus().consumer<CompletePaymentCommand>("CompletePaymentCommand") {
//            launch {
//                logger.info { "Launched handler CompletePaymentCommand ${Thread.currentThread().name}" }
//                awaitBlocking {
//                    logger.info { "Launched handler blocking ${Thread.currentThread().name}" }
//                    paymentCommandHandler.handle(it.body())
//                }
//                logger.info { "Leaving handler CompletePaymentCommand Launched ${Thread.currentThread().name}" }
//            }
//        }
//
//        vertx.eventBus().consumer<RequestPaymentCommand>("RequestPaymentCommand") {
//            launch {
//                logger.info { "Launched handler RequestPaymentCommand ${Thread.currentThread().name}" }
//                awaitBlocking {
//                    paymentCommandHandler.handle(it.body())
//                }
//                logger.info { "Leaving handler RequestPaymentCommand Launched ${Thread.currentThread().name}" }
//            }
//        }
//
//        vertx.eventBus().consumer<PaymentRequestedEvent>("PaymentRequestedEvent") {
//            logger.info { "Launched PaymentRequestedEvent ${Thread.currentThread().name}" }
//            paymentSaga.on(it.body())
//        }
//        vertx.eventBus().consumer<InvoicePaidEvent>("InvoicePaidEvent") {
//            logger.info { "Launched InvoicePaidEvent ${Thread.currentThread().name}" }
//            paymentSaga.on(it.body())
//        }
//
//        vertx.eventBus().consumer<PaymentFailedEvent>("PaymentFailedEvent") {
//            paymentSaga.on(it.body())
//        }
    }
}
