package crocodile8.single_event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Example implementation based on [Channel].
 * In general it's ok but not reliable with different threads / dispatchers.
 */
internal class SingleEventChannel<T>(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
) : SingleEventQueue<T> {

    private val _eventFlow = Channel<T>(capacity = Channel.BUFFERED)
    internal val eventFlow = _eventFlow.receiveAsFlow()

    override var value: T?
        set(value) {
            if (value != null) {
                _eventFlow.trySend(value)
                /*
                scope.launch {
                    _eventFlow.send(value)
                }*/
            }
        }
        get() = null

    /**
     * Adds a new value to the underlying event queue. See [value].
     */
    override fun push(newValue: T) {
        value = newValue
    }

    override suspend fun collect(onEvent: (T) -> Unit) {
        eventFlow.collect { onEvent(it) }
    }
}