package crocodile8.single_event

interface SingleEventQueue<T> : SingleEventCollector<T> {

    var value: T?

    fun push(newValue: T)
}
