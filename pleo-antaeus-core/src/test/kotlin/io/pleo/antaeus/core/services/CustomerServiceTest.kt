package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.context.customer.CustomerRepository
import io.pleo.antaeus.context.customer.CustomerService
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val repository = mockk<CustomerRepository> {
        every { load(404) } returns null
    }

    private val customerService = CustomerService(repository = repository)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }
}
