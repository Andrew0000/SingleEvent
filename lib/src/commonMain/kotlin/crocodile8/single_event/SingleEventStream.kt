package crocodile8.single_event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

//TODO make the queue limited via a constructor parameter.

/**
 * Implementation of the "One-Time Event" pattern.
 * Key features:
 * - It is guaranteed that a pushed even will not be lost.
 * - Many events may be queued until someone collects them.
 * - New value can bu pushed from any thread / dispatcher.
 * - Collection may be happening from any thread / dispatcher.
 * - One collector at a time is the appropriate usage. Sharing is not reliably supported.
 *
 * History of the question and other solutions:
 *
 * https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295
 *
 * https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95
 *
 */
class SingleEventStream<T>(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : SingleEventCollector<T> {

    private val signal: MutableStateFlow<Int> = MutableStateFlow(0)

    private var eventsQueue: List<T> = listOf()

    private val eventsQueueLock = Mutex()

    /**
     * Adds a new value to the underlying event queue.
     * This operation may be asynchronous, it depends on the [scope].
     *
     * Getting the value is not supported.
     */
    var value: T?
        set(value) {
            if (value != null) {
                scope.launch {
                    eventsQueueLock.withLock {
                        eventsQueue += value
                        signal.value = ++signal.value
                    }
                }
            }
        }
        get() = null

    /**
     * See [value]
     */
    fun push(newValue: T) {
        value = newValue
    }

    // Note: the collector function (argument) must be not suspendable to avoid the possibility
    // of additional suspension windows where the collection may be cancelled.
    // Don't use FlowCollector.
    /**
     * Listens and automatically consumes events.
     * Only 1 collection at a time is allowed.
     */
    override suspend fun collect(onEvent: (T) -> Unit) {
        signal.collect {
            eventsQueueLock.withLock {
                eventsQueue.forEach {
                    onEvent(it)
                }
                eventsQueue = listOf()
                // No need to notify the signal
            }
        }
    }
}

/**
 * Provides the immutable version: [SingleEventCollector].
 */
fun <T> SingleEventStream<T>.asCollector(): SingleEventCollector<T> =
    this

interface SingleEventCollector<T> {

    suspend fun collect(onEvent: (T) -> Unit)
}
