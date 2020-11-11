package io.pleo.antaeus.job

import io.pleo.antaeus.context.billing.BillingService
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class BillingJob : Job {
    override fun execute(context: JobExecutionContext) {
        val localTime: LocalDateTime = LocalDateTime.now()
        println("Run QuartzJob at $localTime")
        val billingService: BillingService = context.mergedJobDataMap["billingService"] as BillingService
        billingService.startProcess()

    }
}
