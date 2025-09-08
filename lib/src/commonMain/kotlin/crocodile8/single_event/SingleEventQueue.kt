package crocodile8.single_event

interface SingleEventQueue<T> : SingleEventCollector<T> {

    fun push(newValue: T)
}
