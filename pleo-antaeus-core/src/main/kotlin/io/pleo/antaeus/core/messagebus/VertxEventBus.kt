package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.event.Event


class VertxEventBus(private val eventBus: io.vertx.core.eventbus.EventBus) : EventBus {
    override fun publish(event: Event) {
        eventBus.publish(event::class.simpleName, event)
    }
}