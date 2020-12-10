package ru.spb.isamokhvalov.example

import mu.KLogging
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class MainClass {
    fun process() {
        logger.info { "Hello" }
    }

    private companion object : KLogging()
}


class ClientService(timeToSleep: Long) {
    private val remoteService = VeryRemoteService(timeToSleep)

    fun directCall(): Long {
        return remoteService.process()
    }

    private val fixedThreadPool = Executors.newFixedThreadPool(2)

    fun fixedThreadPoolCall(): Long {
        return fixedThreadPoolFutureCall().get()
    }
    fun fixedThreadPoolFutureCall(): Future<Long> {
        return fixedThreadPool.submit(Callable {
            remoteService.process()
        })
    }
}

class VeryRemoteService(private val timeToSleep: Long) {

    fun process(): Long {
        logger.info { "Start processing with sleep: $timeToSleep" }
        if (timeToSleep != 0L) {
            TimeUnit.SECONDS.sleep(timeToSleep)
            logger.info { "After sleep: $timeToSleep" }
        }
        return System.currentTimeMillis()
    }

    private companion object : KLogging()
}