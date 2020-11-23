package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.commands.Command

interface CommandBus {
    fun send(command: Command)
    suspend fun sendAwait(command: Command)
}