package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.event.Event

interface EventBus {
    fun publish(event: Event)
}