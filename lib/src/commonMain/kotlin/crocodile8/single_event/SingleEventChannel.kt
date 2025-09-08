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

    override fun push(newValue: T) {
        _eventFlow.trySend(newValue)
        /*
        scope.launch {
            _eventFlow.send(value)
        }*/
    }

    override suspend fun collect(onEvent: (T) -> Unit) {
        eventFlow.collect { onEvent(it) }
    }
}