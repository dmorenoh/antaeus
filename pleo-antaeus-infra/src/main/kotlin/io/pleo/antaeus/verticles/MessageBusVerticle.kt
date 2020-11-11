package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.billing.BillingSaga
import io.pleo.antaeus.context.billing.CloseBillingInvoiceCommand
import io.pleo.antaeus.context.billing.StartBillingCommand
import io.pleo.antaeus.context.invoice.InvoiceCommandHandlerOld
import io.pleo.antaeus.context.invoice.InvoiceRepository
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.context.payment.*
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MessageBusVerticle(private val billingSaga: BillingSaga,
                         private val paymentSagaOld: PaymentSagaOld,
                         private val invoiceCommandHandlerOld: InvoiceCommandHandlerOld,
                         private val paymentCommandHandler: PaymentCommandHandler,
                         private val billingCommandHandler: BillingCommandHandler,
                         private val invoiceRepository: InvoiceRepository,
                         private val paymentProvider: PaymentProvider) : CoroutineVerticle() {
    override suspend fun start() {



//        vertx.eventBus().consumer<RequestPaymentCommand>("RequestPaymentCommand") {
//            launch {
////                logger.info { "Is going to handle request payment for ${it.body().invoiceId}" }
//                awaitBlocking {
//                    paymentCommandHandler.handle(it.body())
//                }
////                logger.info { "Finish to handle request payment for ${it.body().invoiceId}" }
//            }
//        }

//        vertx.eventBus().consumer<CompletePaymentCommand>("CompletePaymentCommand") {
//            launch {
//                awaitBlocking {
//                    paymentCommandHandler.handle(it.body())
//                }
//            }
//        }
//
//        vertx.eventBus().consumer<CancelPaymentCommand>("CancelPaymentCommand") {
//            launch {
//                awaitBlocking {
//                    paymentCommandHandler.handle(it.body())
//                }
//            }
//
//        }

        vertx.eventBus().consumer<CreatePaymentCommand>("RequestPaymentCommand") { message ->

            paymentCommandHandler.handle(message.body())
//            val newPayment = Payment.create(message.body())
//            paymentsMap[newPayment.transactionId] = newPayment
//
//            vertx.eventBus().send(PaymentRequestedEvent::class.simpleName, PaymentRequestedEvent(newPayment
//                    .transactionId, newPayment.invoiceId, newPayment.billingProcessId))
        }

        vertx.eventBus().consumer<CompletePaymentCommand>("CompletePaymentCommand") { message ->

            paymentCommandHandler.handle(message.body())
//            paymentsMap[message.body().transactionId]?.complete()
//                    ?.let {
//                        paymentsMap.replace(it.transactionId, it)
//                        vertx.eventBus().send(PaymentCompletedEvent::class.simpleName,
//                                PaymentCompletedEvent(it.transactionId, it.invoiceId, it.billingProcessId))
//                    }

        }

        vertx.eventBus().consumer<CancelPaymentCommand>("CancelPaymentCommand") { message ->
            paymentCommandHandler.handle(message.body())
//            paymentsMap[message.body().transactionId]?.cancel(message.body().paymentCancellationReason)
//                    ?.let {
//                        paymentsMap.replace(it.transactionId, it)
//                        vertx.eventBus().send(PaymentCanceledEvent::class.simpleName,
//                                PaymentCanceledEvent(it.transactionId, it.invoiceId, message.body()
//                                        .paymentCancellationReason, it.billingProcessId))
//                    }

        }

        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") { message ->
            launch {
                invoiceCommandHandlerOld.handle(message.body())
            }
        }


//        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") { message ->
//
//            launch() {
//                logger.info { "Not blocking payment for ${message.body().invoiceId}" }
//                runCatching {
//                    awaitBlocking {
//
//
//                        logger.info { "Launch coroutine to handle payment for ${message.body().invoiceId}" }
//                        val invoice = invoiceRepository.load(message.body().invoiceId)
//                                ?: throw InvoiceNotFoundException(id = message.body().invoiceId)
//                        val invoicePaid = invoice.pay(paymentProvider)
//                        invoiceRepository.update(invoicePaid)
//                        invoicePaid
//                    }
//                }.onSuccess {
//                    logger.info { "Invoice paid persisted for invoice: ${it.id} and payment ${message.body().transactionId}" }
//                    vertx.eventBus().send("InvoicePaidEvent", InvoicePaidEvent(message.body().transactionId, it.id))
//                }.onFailure {
//                    logger.info {
//                        "Invoice paid failed for invoice: ${message.body().invoiceId} and payment ${message
//                                .body()
//                                .transactionId}"
//                    }
//                    vertx.eventBus().send("PaymentFailedEvent", PaymentFailedEvent(message.body().transactionId, message
//                            .body().invoiceId, it))
//                }
//
//                logger.info { "Finish to handle payment for ${message.body().invoiceId}" }
//            }
//        }


//
//        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") { message ->
//
//            launch(vertx.dispatcher()) {
//                logger.info { "Not blocking payment for ${message.body().invoiceId}" }
//                runCatching {
//                    awaitBlocking {
//                        if (message.body().invoiceId == 1 || message.body().invoiceId == 31 || message.body().invoiceId == 51) {
//                            logger.info { "sleeping" }
//
//                        }
//
//                        logger.info { "Launch coroutine to handle payment for ${message.body().invoiceId}" }
//                        val invoice = invoiceRepository.load(message.body().invoiceId)
//                                ?: throw InvoiceNotFoundException(id = message.body().invoiceId)
//                        val invoicePaid = invoice.pay(paymentProvider)
//                        invoiceRepository.update(invoicePaid)
//                        invoicePaid
//                    }
//                }.onSuccess {
//                    logger.info { "Invoice paid persisted for invoice: ${it.id}" }
//                    vertx.eventBus().send("InvoicePaidEvent", InvoicePaidEvent(message.body().transactionId, it.id))
//                }.onFailure {
//                    vertx.eventBus().send("InvoicePaidEvent", PaymentFailedEvent(message.body().transactionId, message
//                            .body().invoiceId, it))
//                }
//
//                logger.info { "Finish to handle payment for ${message.body().invoiceId}" }
//            }
//        }

//
//        vertx.eventBus().consumer<PayInvoiceCommand>("PayInvoiceCommand") {
//            logger.info { "Is going to handle payment for ${it.body().invoiceId}" }
//            launch {
//                logger.info { "Launch coroutine to handle payment for ${it.body().invoiceId}" }
//                awaitBlocking {
//                    invoiceCommandHandler.handle(it.body())
//                }
//                logger.info { "Finish to handle payment for ${it.body().invoiceId}" }
//            }
//        }


        vertx.eventBus().consumer<StartBillingCommand>("StartBillingCommand") {

                    billingCommandHandler.handle(it.body())


        }

        vertx.eventBus().consumer<CloseBillingInvoiceCommand>("CloseBillingInvoiceCommand") {

                    billingCommandHandler.handle(it.body())


        }
//        super.start()
    }


}