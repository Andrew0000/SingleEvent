package crocodile8.single_event

interface SingleEventCollector<T> {

    suspend fun collect(onEvent: (T) -> Unit)
}