package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.commands.Command
import io.vertx.core.eventbus.EventBus
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


class VertxCommandBus(private val eventBus: EventBus) : CommandBus {
    override fun send(command: Command): CompletionStage<Void> {
        val cs: CompletableFuture<Void> = CompletableFuture<Void>()

        eventBus.request<Void>(command::class.simpleName, command) { event ->
            if (event.failed())
                cs.completeExceptionally(event.cause())
            else
                cs.complete(event.result() as Nothing?)
        }
        return cs
    }

}

