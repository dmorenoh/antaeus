package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.commands.Command
import io.vertx.core.eventbus.EventBus


class VertxCommandBus(private val eventBus: EventBus) : CommandBus {
    override fun send(command: Command) {
        eventBus.send(command::class.simpleName, command)
    }
}

