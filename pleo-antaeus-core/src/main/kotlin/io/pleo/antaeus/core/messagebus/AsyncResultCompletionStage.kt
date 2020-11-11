package io.pleo.antaeus.core.messagebus

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer

class AsyncResultCompletionStage {
    companion object {
        fun toCompletionStage(completionConsumer: Consumer<Handler<AsyncResult<Void>>?>): CompletionStage<Void> {
            val cs = CompletableFuture<Void>()
            try {
                completionConsumer.accept(Handler { ar: AsyncResult<Void> ->
                    if (ar.succeeded()) {
                        cs.complete(ar.result())
                    } else {
                        cs.completeExceptionally(ar.cause())
                    }
                })
            } catch (e: java.lang.Exception) {
                // unsure we need this ?
                cs.completeExceptionally(e)
            }
            return cs
        }

        fun toCompletionStage2(c: (Handler<AsyncResult<Void>>) -> Unit): CompletionStage<Void> {
            val cs = CompletableFuture<Void>()
            try {
                c(Handler { ar: AsyncResult<Void> ->
                    if (ar.succeeded()) {
                        println("Complete as success")
                        cs.complete(ar.result())
                    } else {
                        println("Complete as failure")
                        cs.completeExceptionally(ar.cause())
                    }
                })
            } catch (e: java.lang.Exception) {
                // unsure we need this ?
                cs.completeExceptionally(e)
            }
            return cs
        }
    }


}