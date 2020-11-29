package io.pleo.antaeus.job

import io.pleo.antaeus.context.billing.BillingService
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime

class BillingJob : Job {

    private val logger = KotlinLogging.logger {}

    override fun execute(context: JobExecutionContext) {

        val localTime: LocalDateTime = LocalDateTime.now()
        logger.info { "Run QuartzJob at $localTime" }
        val billingService: BillingService = context.mergedJobDataMap["billingService"] as BillingService
        billingService.startProcess()

    }
}
