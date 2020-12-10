package ru.spb.isamokhvalov.example

import com.google.common.util.concurrent.ThreadFactoryBuilder
import mu.KLogging
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class ClientServiceTest {

    private val testable = ClientService(2)
    private val pool = Executors.newFixedThreadPool(10, ThreadFactoryBuilder().setNameFormat("client-pool-%d").build())

    private var start: Long = 0
    private var end: Long = 0


    @Before
    fun setUp() {
        logger.info { "Start" }
        start = System.currentTimeMillis()
    }

    @After
    fun tearDown() {
        end = System.currentTimeMillis()
        val duration = Duration.ofMillis(end - start)
        logger.info { "duration: $duration" }
    }

    @Test
    fun manyRequests() {

        val result = (1..10)
            .map {
//                pool.submit(Callable{ testable.directCall() })
//                pool.submit(Callable { testable.fixedThreadPoolCall() })
                testable.fixedThreadPoolFutureCall()
            }.map { it.get() }
        logger.info { "result: $result" }
    }

    @Test
    fun tooManyRequest() {
        val start = System.currentTimeMillis()

        val result = (1..10).map {
            CompletableFuture.supplyAsync({
                testable.fixedThreadPoolCall()
            }, pool)
        }.map { it.get() }

        logger.info { "result: $result" }
    }


    @Test
    fun name() {
        val future = CompletableFuture.supplyAsync({
            testable.fixedThreadPoolCall()
        }, pool)
        logger.info { "after submit" }
        val lastFuture = future.thenApplyAsync({ //[ForkJoinPool.commonPool-worker-1]
//        val lastFuture = future.thenApply({ //client-pool-0]
            val secondCall = testable.fixedThreadPoolCall()
            logger.info { "it: $it, secondCall: $secondCall" }
            it + secondCall
        })
        logger.info { "after thenApply" }
        val result = lastFuture.get()
//        logger.info { "result: $result" }
    }

    @Test
    fun testApplyVsApplyAsync() {
        val strategy = 2
        val singleThreadPool = Executors.newSingleThreadExecutor()
        val results = (1..3).map {
            CompletableFuture.supplyAsync({
                testable.fixedThreadPoolCall()
            }, singleThreadPool)
        }.map {
            val function: (t: Long) -> Long = { firstCallResult ->
                val secondCallResult = testable.fixedThreadPoolCall()
                logger.info { "firstCallResult: $firstCallResult, secondCallResult: $secondCallResult" }
                secondCallResult + firstCallResult
            }
            when (strategy) {
                1 -> it.thenApply(function)
                2 -> it.thenApplyAsync(function)
                else -> throw  IllegalArgumentException("Strategy $strategy not supported")
            }
        }.map { it.get() }

        logger.info { "results: $results" }
    }

    private companion object : KLogging()
}
