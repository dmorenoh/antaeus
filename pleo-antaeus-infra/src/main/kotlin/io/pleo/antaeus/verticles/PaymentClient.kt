package io.pleo.antaeus.verticles

import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.vertx.circuitbreaker.CircuitBreaker

class PaymentClient(
        val paymentProvider: PaymentProvider,
        val circuitBreaker: CircuitBreaker) {

//    fun charge(invoice: Invoice): Boolean {
//        val breaker = circuitBreaker.execute<Boolean> { future ->
//            runCatching {
//                paymentProvider.charge(invoice)
//            }.onFailure {
//                if (it is NetworkException)
//                    future.fail("Network error")
//                future.complete()
//            }.onSuccess {
//                future.complete(it)
//            }
//        }
//        if (breaker.succeeded())
//    }
}