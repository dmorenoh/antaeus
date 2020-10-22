package io.pleo.antaeus.context.invoice.dal

import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class InvoiceDalImpl(val antaeusDal: AntaeusDal) : InvoiceDal {
    override fun update(invoice: Invoice) = antaeusDal.updateInvoice(invoice)


    override fun fetchInvoices(): List<Invoice> {
        TODO("Not yet implemented")
    }

    override fun fetchInvoice(id: Int): Invoice {
        TODO("Not yet implemented")
    }
}