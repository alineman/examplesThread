package ru.spb.isamokhvalov.example

import mu.KLogging
import org.junit.Test
import java.lang.IllegalArgumentException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class CallableTest {

    private val pool = Executors.newFixedThreadPool(2)

    @Test
    fun checkIsDone() {
        val future = pool.submit(DurationCallable())
        logger.info { "After submit" }
        while (!future.isDone) {
            logger.info { "Waiting is done" }
            TimeUnit.MILLISECONDS.sleep(500)
        }
        val result = future.get()
        logger.info { "result: $result" }
    }

    @Test
    fun checkGet() {
        val strategy = 3
        val future = pool.submit(Callable {
            val start = System.nanoTime()
            TimeUnit.SECONDS.sleep(1)
            val end = System.nanoTime()
            val total = end - start
            logger.info { "total: $total" }
            total
        })
        logger.info { "After submit" }

        val result = when (strategy) {
            1 -> future.get()
            2 -> future.get(500, TimeUnit.MILLISECONDS)
            3 -> {
                try {
                    future.get(500, TimeUnit.MILLISECONDS)
                } catch (e: TimeoutException) {
                    logger.error(e) { "Time is end..." }
                    future.get(500, TimeUnit.MILLISECONDS)
                }
            }
            else -> throw  IllegalArgumentException("Strategy $strategy not supported")
        }
        logger.info { "result: $result" }
    }


    @Test
    fun manyPushToPool() {
        val callable = DurationCallable()
        val features = (1..3).map {
            pool.submit(callable)
        }
        logger.info { "After submit" }
        logger.info { "features: $features" }
        val result = features.map { it.get() }
        logger.info { "result: $result" }
    }

    private companion object : KLogging()
}

class DurationCallable(private val duration: Long = 1) : Callable<Long> {
    override fun call(): Long {
        val start = System.nanoTime()
        TimeUnit.SECONDS.sleep(duration)
        val end = System.nanoTime()
        val total = end - start
        logger.info { "total: $total" }
        return total
    }

    private companion object : KLogging()
}
