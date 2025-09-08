package crocodile8.single_event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SingleEventStreamTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val stream = SingleEventStream<Int>(CoroutineScope(testDispatcher))
    // private val stream = SingleEventChannel<Int>(CoroutineScope(testDispatcher))

    @Test
    fun `When events sent - they are received`() = runTest {
        val result = mutableListOf<Int>()
        val job1 = launch(testDispatcher) {
            stream.collect { result += it }
        }
        stream.push(1)
        stream.push(2)
        stream.push(3)

        testDispatcher.scheduler.advanceUntilIdle()
        job1.cancel()

        assertEquals(
            listOf(1, 2, 3),
            result.sorted()
        )
    }

    @Test
    fun `When events sent but collected later - they are received`() = runTest {
        val result = mutableListOf<Int>()
        val job1 = launch(testDispatcher) {
            stream.collect { result += it }
        }
        stream.push(1)
        stream.push(2)
        stream.push(3)

        testDispatcher.scheduler.advanceUntilIdle()
        job1.cancel()

        stream.push(4)
        stream.push(5)
        stream.push(6)

        val job2 = launch(testDispatcher) {
            stream.collect { result += it }
        }

        testDispatcher.scheduler.advanceUntilIdle()
        job2.cancel()

        assertEquals(
            listOf(1, 2, 3, 4, 5, 6),
            result.sorted()
        )
    }

    @Test
    fun `When events sent and collected second time - they are received once`() = runTest {
        val result = mutableListOf<Int>()
        val job1 = launch(testDispatcher) {
            stream.collect { result += it }
        }
        stream.push(1)
        stream.push(2)
        stream.push(3)

        testDispatcher.scheduler.advanceUntilIdle()
        job1.cancel()

        val job2 = launch(testDispatcher) {
            stream.collect { result += it }
        }

        testDispatcher.scheduler.advanceUntilIdle()
        job2.cancel()

        assertEquals(
            listOf(1, 2, 3),
            result.sorted()
        )
    }

    @Test
    fun `When events sent and received from different dispatchers and threads - nothing is lost`() = runTest {
        val asyncDispatcher2 = Dispatchers.IO
        val asyncDispatcher3 = Dispatchers.IO

        // Many repetitions because it's concurrency-related
        // and theoretical issues may appear not from the 1st shot.
        repeat(1000) {
            val lock = Channel<Unit>(capacity = 100)
            val result = mutableListOf<Int>()

            stream.push(1)
            stream.push(2)
            val job1 = launch(testDispatcher) {
                stream.collect { result += it }
            }
            launch(asyncDispatcher2) {
                stream.push(3)
                stream.push(32)
                lock.send(Unit)
            }
            stream.push(4)
            stream.push(42)

            job1.cancel()

            val job2 = launch(testDispatcher) {
                stream.collect { result += it }
            }

            launch(asyncDispatcher3) {
                stream.push(5)
                lock.send(Unit)
            }
            stream.push(6)

            repeat(2) {
                lock.receive()
            }
            job2.cancelAndJoin()

            assertEquals(
                listOf(1, 2, 3, 4, 5, 6, 32, 42),
                result.sorted()
            )
        }
    }
}
