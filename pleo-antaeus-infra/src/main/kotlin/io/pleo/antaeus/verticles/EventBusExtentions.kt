package io.pleo.antaeus.verticles

import io.pleo.antaeus.core.messagebus.GenericCodec
import io.vertx.core.eventbus.EventBus
import kotlin.reflect.KClass


fun <T : Any> EventBus.consumerOf(commandClass: KClass<T>) = this.consumer<T>(commandClass.simpleName)

fun <T : Any> EventBus.registerCodec(clazz: Class<T>) {
    this.registerDefaultCodec(clazz, GenericCodec(clazz))
}

