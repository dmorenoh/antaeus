package io.pleo.antaeus.verticles

import io.pleo.antaeus.job.BillingJob
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.triggers.CronTriggerImpl


class QuarzVerticle(private val scheduler: Scheduler, private val jobKey: JobKey, var cronExpression: String) :
        AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        if (scheduler.checkExists(jobKey)) {
            startPromise.complete()
            return
        }

        val jobDetail = JobDetailImpl()
        jobDetail.jobClass = BillingJob::class.java
        jobDetail.key = jobKey

        val trigger = CronTriggerImpl()
        trigger.name = jobKey.toString() + "Trigger"
        trigger.cronExpression = cronExpression

        scheduler.scheduleJob(jobDetail, trigger)
        startPromise.complete()
    }
}