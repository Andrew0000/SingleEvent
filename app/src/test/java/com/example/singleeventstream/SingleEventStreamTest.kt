package com.example.singleeventstream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class SingleEventStreamTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val asyncDispatcher = UnconfinedTestDispatcher()

    private val stream = SingleEventStream<Int>(CoroutineScope(asyncDispatcher))

    @Test
    fun `When events sent - they are received`() = runTest {
        val result = mutableListOf<Int>()
        val job1 = launch(asyncDispatcher) {
            stream.collect { result += it }
        }
        stream.value = 1
        stream.value = 2
        stream.value = 3

        asyncDispatcher.scheduler.advanceUntilIdle()
        job1.cancel()

        assertArrayEquals(
            intArrayOf(1, 2, 3),
            result.toIntArray()
        )
    }

    @Test
    fun `When events sent but collected later - they are received`() = runTest {
        val result = mutableListOf<Int>()
        val job1 = launch(asyncDispatcher) {
            stream.collect { result += it }
        }
        stream.value = 1
        stream.value = 2
        stream.value = 3

        asyncDispatcher.scheduler.advanceUntilIdle()
        job1.cancel()

        stream.value = 4
        stream.value = 5
        stream.value = 6

        val job2 = launch(asyncDispatcher) {
            stream.collect { result += it }
        }

        asyncDispatcher.scheduler.advanceUntilIdle()
        job2.cancel()

        assertArrayEquals(
            intArrayOf(1, 2, 3, 4, 5, 6),
            result.toIntArray()
        )
    }
}
