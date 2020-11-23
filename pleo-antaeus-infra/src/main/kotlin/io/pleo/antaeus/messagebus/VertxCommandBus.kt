package io.pleo.antaeus.messagebus

import io.pleo.antaeus.core.commands.Command
import io.pleo.antaeus.core.messagebus.CommandBus
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.awaitResult


class VertxCommandBus(private val eventBus: EventBus) : CommandBus {
    override fun send(command: Command) {
        eventBus.send(command::class.simpleName, command)
    }

    override suspend fun sendAwait(command: Command) {
        awaitResult<Message<Void>> {
            eventBus.request(command::class.java.simpleName, command, it)
        }
    }
}

