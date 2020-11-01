package io.pleo.antaeus.job

import io.pleo.antaeus.context.billing.BillingCommandHandler
import io.pleo.antaeus.context.billing.StartBillingCommand
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime
import java.util.*

class BillingJob (private val commandHandler: BillingCommandHandler) : Job {
    override fun execute(context: JobExecutionContext?) {
        val localTime: LocalDateTime = LocalDateTime.now()
        println("Run QuartzJob at $localTime")
        commandHandler.handle(StartBillingCommand(UUID.randomUUID()))
    }
}
