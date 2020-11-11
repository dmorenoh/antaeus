package io.pleo.antaeus.app

import io.pleo.antaeus.context.invoice.AccountBalanceException
import io.pleo.antaeus.context.invoice.PayInvoiceCommand
import io.pleo.antaeus.core.messagebus.GenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.CountDownLatch


class FancyTest {

    lateinit var vertx: Vertx

    @BeforeEach
    fun setup() {

        vertx = Vertx.vertx()

        Vertx.vertx().exceptionHandler {
            print("doing something nice")
        }

        vertx.eventBus().registerDefaultCodec(PayInvoiceCommand::class.java,
                GenericCodec(PayInvoiceCommand::class.java))




    }


    @Test
    fun `should process invoice payment`() {

        val latch = CountDownLatch(1)
        val regLatch = CountDownLatch(1)


        vertx.eventBus().consumer<PayInvoiceCommand>("address") {
            println("ITS HERE")
//            it.fail(0, "fallo")
            throw AccountBalanceException("test")
        }.completionHandler{
            if (it.succeeded())
                regLatch.countDown()
        }
        regLatch.await()
        vertx.eventBus().request<Void>("address", PayInvoiceCommand(UUID.randomUUID(), 1), DeliveryOptions()
                .setSendTimeout(100)) {
            if (it.failed())
                println("failed")
            else
                println("success")
            assert(it.failed())
            latch.countDown()
        }

        latch.await()

//        vertx.eventBus().send("address", PayInvoiceCommand(UUID.randomUUID(), 1))


        Thread.sleep(10000)
    }
}