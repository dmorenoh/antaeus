package io.pleo.antaeus.app

import arrow.core.Either
import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.core.event.Event
import io.pleo.antaeus.verticles.createConsumer
import io.pleo.antaeus.verticles.registerCodec
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

open class AppVerticle : CoroutineVerticle() {

    fun registerCommands(commands: List<KClass<out Command>>,
                         handler: suspend (T: Command) -> Either<Throwable, Event>) {

        val eventBus = vertx.eventBus()

        commands.forEach { command ->
            eventBus.registerCodec(command.java)
                    .createConsumer(command)
                    .handler { message ->
                        launch {
                            handler(message.body())
                                    .fold(
                                            ifLeft = { error -> handleFailure(message, error) },
                                            ifRight = { event -> handleSuccess(message, event) }
                                    )
                        }
                    }
        }

    }

    fun registerEventHandlers(events: List<KClass<out Event>>,
                              handler: suspend (T: Event) -> Unit) {
        val eventBus = vertx.eventBus()

        events.forEach {
            eventBus
                    .createConsumer(it)
                    .handler { message ->
                        launch { handler(message.body()) }
                    }
        }
    }

    private fun handleSuccess(message: Message<out Command>, it: Event) {
        val eventBus = vertx.eventBus()
        message.reply("done")
        eventBus.send(it::class.simpleName, it)
    }

    private fun handleFailure(message: Message<out Command>, error: Throwable) {
        message.fail(1, error::class.simpleName)
    }
}