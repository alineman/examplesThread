package ru.spb.isamokhvalov.example

import mu.KLogging
import org.junit.Test
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainClassTest {

    @Test
    fun simpleRunThread() {
        logger.info { "Outside Thread" }

        Thread {
            logger.info { "In Thread" }
        }.run()
    }


    @Test
    fun simpleStartThread() {
        logger.info { "Outside Thread" }

        Thread {
            logger.info { "In Thread" }
        }.start()

        TimeUnit.SECONDS.sleep(1)
    }

    @Test
    fun sleepingThread() {
        logger.info { "Before execute" }
        SleepingThread(2).start()
        SleepingThread(3).start()
        SleepingThread(4).start()
        logger.info { "After execute" }

        TimeUnit.SECONDS.sleep(4)
        logger.info { "End" }
    }

    @Test
    fun waitStartingThread() {
        logger.info { "Before execute" }
        val thread = SleepingThread(2)
        var i = 5
        while (i > 0) {
            logger.info { "state: ${thread.state}\tisAlive: ${thread.isAlive}\tisInterrupted: ${thread.isInterrupted}" }
            if (i == 5) {
                thread.start()
                logger.info { "After start" }
            }
            i--
            TimeUnit.SECONDS.sleep(1)
        }
    }

    @Test
    fun modifyVariable() {
        var result: Long = 0

        val thread = Thread {
            val start = System.nanoTime()
            TimeUnit.SECONDS.sleep(1)
            val end = System.nanoTime()
            val total = end - start
            result = total
        }
        thread.start()
        logger.info { "After start" }
        while (thread.isAlive) {
            logger.info { "result: $result" }
            TimeUnit.MILLISECONDS.sleep(500)
        }
        logger.info { "result: $result" }
    }

    @Test
    fun modifyVariableJoin() {
        var result: Long = 0

        val thread = Thread {
            val start = System.nanoTime()
            TimeUnit.SECONDS.sleep(1)
            val end = System.nanoTime()
            val total = end - start
            result = total
        }
        thread.start()
        logger.info { "After start" }
        logger.info { "result: $result" }
        thread.join()
        logger.info { "result: $result" }
    }

    @Test
    fun runFixedThreadPool() {
        logger.info { "Outside Thread" }
        val pool = Executors.newFixedThreadPool(1)
        pool.submit({
            logger.info { "In Thread" }
        })

        TimeUnit.SECONDS.sleep(3)
    }

    @Test
    fun sleepingThreadInPool() {
        val strategy = 1

        val pool = Executors.newFixedThreadPool(2)
        val threads = listOf(
            SleepingThread(1),
            SleepingThread(2),
            SleepingThread(3)
        )
        logger.info { "Before submit" }
        threads.forEach { pool.submit(it) }
        logger.info { "After submit" }

        when (strategy) {
            1 -> TimeUnit.SECONDS.sleep(4)
            2 -> {
                pool.shutdown()
                while (!pool.isTerminated) {
                    logger.info { "Wait shutdown..." }
                    TimeUnit.SECONDS.sleep(1)
                }
            }
            3 -> {
                pool.shutdown()
                while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
                    logger.info { "awaitTermination..." }
                }
            }
            else -> throw  IllegalArgumentException("Strategy $strategy not supported")
        }

        logger.info { "End." }

    }


    private companion object : KLogging()
}

class SleepingThread(private val sleep: Long) : Thread() {

    override fun run() {
        logger.info { "Start processing with sleep: $sleep" }
        if (sleep != 0L) {
            TimeUnit.SECONDS.sleep(sleep)
            logger.info { "After sleep: $sleep" }
        }
        return
    }

    private companion object : KLogging()
}