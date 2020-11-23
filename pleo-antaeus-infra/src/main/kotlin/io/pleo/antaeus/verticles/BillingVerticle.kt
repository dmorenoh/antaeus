package io.pleo.antaeus.verticles

import arrow.core.Either
import io.pleo.antaeus.context.billing.*
import io.pleo.antaeus.context.payment.PaymentCanceledEvent
import io.pleo.antaeus.context.payment.PaymentCompletedEvent
import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.core.event.Event
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch
import mu.KotlinLogging

class BillingVerticle(private val billingCommandHandler: BillingCommandHandler,
                      private val billingSaga: BillingSaga) : CoroutineVerticle() {

    private val logger = KotlinLogging.logger {}


    private val commandMessage = listOf(
            StartBillingCommand::class,
            CloseBillingInvoiceCommand::class)

    private val eventMessage = listOf(
            BillingStartedEvent::class,
            BillingCompletedEvent::class)

    private val messages = listOf(StartBillingCommand::class.java,
            BillingStartedEvent::class.java,
            CloseBillingInvoiceCommand::class.java,
            BillingCompletedEvent::class.java)

    override suspend fun start() {
        val eventBus = vertx.eventBus()

        commandMessage.forEach {
            eventBus.registerCodec(it.java)
            eventBus.consumerOf(it)
                    .handler { message ->
                        launch { attemptExecute(message) }
                    }
        }


        eventMessage.forEach {
            eventBus.registerCodec(it.java)
        }


        listOf(BillingStartedEvent::class, PaymentCanceledEvent::class, PaymentCompletedEvent::class)
                .forEach {
                    eventBus.consumerOf(it)
                            .handler { message ->
                                handleEvent(message.body())
                            }
                }


    }


    suspend fun attemptExecute(message: Message<out Command>) = handle(message.body()).fold(
            ifLeft = { error -> message.fail(1, error::class.simpleName) },
            ifRight = { message.reply("done") }
    )

    suspend fun handle(command: Command): Either<Throwable, Event> = when (command) {
        is StartBillingCommand -> billingCommandHandler.handleFun(command)
        is CloseBillingInvoiceCommand -> billingCommandHandler.handleFun(command)
        else -> Either.left(RuntimeException("conflict"))
    }

    fun handleEvent(event: Event) {
        logger.info { "event : ${event::class.java}" }
        when (event) {
            is BillingStartedEvent -> billingSaga.on(event)
            is PaymentCompletedEvent -> billingSaga.on(event)
            is PaymentCanceledEvent -> billingSaga.on(event)
        }
    }

}