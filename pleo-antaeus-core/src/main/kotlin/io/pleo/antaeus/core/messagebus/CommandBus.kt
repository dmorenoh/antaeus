package io.pleo.antaeus.core.messagebus

import io.pleo.antaeus.core.commands.Command
import java.util.concurrent.CompletionStage

interface CommandBus {
    fun send(command: Command): CompletionStage<Void>
}