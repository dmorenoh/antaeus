package io.pleo.antaeus.context.invoice


import io.pleo.antaeus.context.payment.external.CurrencyExchangeProvider
import io.pleo.antaeus.context.payment.external.PaymentProvider
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.messagebus.EventBus
import io.vertx.kotlin.coroutines.awaitBlocking
import mu.KotlinLogging

class InvoiceCommandHandlerOld(private val repository: InvoiceRepository,
                               private val paymentProvider: PaymentProvider,
                               private val currencyExchangeProvider: CurrencyExchangeProvider,
                               private val eventBus: EventBus) {

    private val logger = KotlinLogging.logger {}

    suspend fun handle(command: PayInvoiceCommand) {

        runCatching {
            awaitBlocking {

                val invoice = repository.load(command.invoiceId)
                        ?: throw InvoiceNotFoundException(id = command.invoiceId)

                val paidInvoice = invoice.pay(paymentProvider)
                repository.update(paidInvoice)
            }
            eventBus.publish(InvoicePaidEvent(
                    transactionId = command.transactionId,
                    invoiceId = command.invoiceId))
        }.onFailure {
            eventBus.publish(PaymentFailedEvent(
                    invoiceId = command.invoiceId,
                    transactionId = command.transactionId,
                    throwable = it))
        }
    }

    suspend fun handle(command: FixCurrencyInvoiceCommand) {
        runCatching {
            awaitBlocking {
                val invoice = repository.load(command.invoiceId)
                        ?: throw InvoiceNotFoundException(id = command.invoiceId)
                val fixedInvoice = invoice.fixCurrency(currencyExchangeProvider)
                repository.update(fixedInvoice)
            }

            eventBus.publish(InvoiceCurrencyUpdatedEvent(invoiceId = command.invoiceId))

        }.onFailure {
            logger.warn { "No possible to fix currency for invoice ${command.invoiceId}" }
        }
    }


}