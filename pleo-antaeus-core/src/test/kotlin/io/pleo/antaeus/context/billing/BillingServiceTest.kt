package io.pleo.antaeus.context.billing

import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.context.customer.Customer
import io.pleo.antaeus.context.invoice.Invoice
import io.pleo.antaeus.context.invoice.InvoiceService
import io.pleo.antaeus.context.invoice.InvoiceStatus
import io.pleo.antaeus.core.messagebus.CommandBus
import io.pleo.antaeus.core.value.Currency
import io.pleo.antaeus.core.value.Money
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class BillingServiceTest {
    companion object {
        val TEN_EURO = Money(BigDecimal.TEN, Currency.EUR)
    }

    private val commandBus = mockk<CommandBus>(relaxed = true)
    private val invoiceService = mockk<InvoiceService>(relaxed = true)
    private val billingRepository = mockk<BillingRepository>(relaxed = true)
    private var billingService = BillingService(billingRepository = billingRepository, invoiceService = invoiceService,
            commandBus =
            commandBus)

    @Test
    fun `should create billing when requested to be create a new one`() {

        val listOfInvoice = listOf(1, 2, 3)
        val result = runBlocking {
            billingService.execute(StartBillingCommand(listOfInvoice))
        }
        result.shouldBeRight { event ->
            assert(event is BillingStartedEvent)
            val billingStartedEvent = event as BillingStartedEvent
            assert(billingStartedEvent.invoices.containsAll(listOfInvoice))
        }
        verify { billingRepository.save(ofType(Billing::class)) }
    }

    @Test
    fun `should fail when closing billing that does not exist`() {
        val billingId = UUID.randomUUID()
        every { billingRepository.load(billingId) } returns null
        val result = runBlocking {
            billingService.execute(CloseBillingInvoiceCommand(billingId, 1))
        }
        result.shouldBeLeft()
    }

    @Test
    fun `should return BillingInvoiceCompletedEvent when closing billing and is not completed yet`() {
        val expectedBilling = Billing.create(StartBillingCommand(listOf(1, 2, 3)))
        every { billingRepository.load(expectedBilling.processId) } returns expectedBilling
        val result = runBlocking {
            billingService.execute(CloseBillingInvoiceCommand(expectedBilling.processId, 1))
        }
        result.shouldBeRight { event ->
            assert(event is BillingInvoiceCompletedEvent)
        }
        assert(expectedBilling.status == BillingStatus.STARTED)
        assert(expectedBilling.invoices[1]?.invoiceStatus == BillingInvoiceStatus.PROCESSED)
    }

    @Test
    fun `should return BillingCompletedEvent when closing billing and is not completed yet`() {
        val expectedBilling = Billing.create(StartBillingCommand(listOf(1, 2, 3)))
        expectedBilling.invoices[1]?.invoiceStatus = BillingInvoiceStatus.PROCESSED
        expectedBilling.invoices[2]?.invoiceStatus = BillingInvoiceStatus.PROCESSED

        every { billingRepository.load(expectedBilling.processId) } returns expectedBilling
        val result = runBlocking {
            billingService.execute(CloseBillingInvoiceCommand(expectedBilling.processId, 1))
        }
        result.shouldBeRight { event ->
            assert(event is BillingInvoiceCompletedEvent)
        }
    }

    @Test
    fun `should do nothing when no pending invoices to process`() {
        every { invoiceService.fetchAllPending() } returns emptyList()
        billingService.startProcess()
        verify(exactly = 0) { commandBus.send(any()) }
    }

    @Test
    fun `should process when pending invoices found`() {
        val customer = Customer(1, Currency.EUR)
        every { invoiceService.fetchAllPending() } returns listOf(
                Invoice(1, customer, TEN_EURO, InvoiceStatus.PENDING, 1),
                Invoice(2, customer, TEN_EURO, InvoiceStatus.PENDING, 1),
                Invoice(3, customer, TEN_EURO, InvoiceStatus.PENDING, 1)
        )
        billingService.startProcess()
        verify(exactly = 1) { commandBus.send(StartBillingCommand(listOf(1, 2, 3))) }
    }
}