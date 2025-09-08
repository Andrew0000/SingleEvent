package crocodile8.single_event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of the "One-Time Event" pattern.
 * Key features:
 * - It is guaranteed that a pushed even will not be lost.
 * - Many events may be queued until someone collects them.
 * - New event can bu pushed from any thread / dispatcher.
 * - Collection may be happening from any thread / dispatcher.
 * - Order of events is no guaranteed if pushed from multiple threads / dispatchers.
 * - Only 1 collector at a time is supported.
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
) : SingleEventQueue<T> {

    private val signal: MutableStateFlow<Int> = MutableStateFlow(0)

    //TODO improvement: make the queue limited via a constructor parameter.
    private var eventsQueue: List<T> = listOf()

    private val eventsQueueLock = Mutex()

    /**
     * Adds [newValue] to the underlying event queue.
     * This operation may be asynchronous, it depends on the [scope].
     */
    override fun push(newValue: T) {
        scope.launch {
            eventsQueueLock.withLock {
                eventsQueue += newValue
                signal.value = ++signal.value
            }
        }
    }

    /**
     * Listens and automatically consumes events.
     * Only 1 collection at a time is allowed.
     *
     * Note: the collector function (argument) must be not suspendable to avoid the possibility
     * of additional suspension windows where the collection may be cancelled,
     * so don't use FlowCollector.
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
 * Provides a read-only version: [SingleEventCollector].
 */
fun <T> SingleEventStream<T>.asReadOnly(): SingleEventCollector<T> =
    this
