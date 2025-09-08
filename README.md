Implementation of the "One-Time Event" pattern for Android and Kotlin Multiplatform.  

**History of the question and other solutions**  
Why Channels may be not reliable:  
https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295  
https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95  

Google's approach (explicitly consume events):  
https://developer.android.com/topic/architecture/ui-layer/events#consuming-trigger-updates  

**SingleEventStream**  
Key features:
- It is guaranteed that a pushed even will not be lost.
- Many events may be queued until someone collects them.
- New event can bu pushed from any thread / dispatcher.
- Collection may be happening from any thread / dispatcher.
- Order of events is no guaranteed if pushed from multiple threads / dispatchers.

Limitations:
- Only 1 collector at a time is supported.

How and why it works:  
Events are stored in the internal queue. Collector automatically and atomically consumes the queue.
It's partially similar to what Google recommends (notify about the consumption) but automatic.

**Usage example**  
The library is not published. Basically it's just 1 class so you can copy it to your project.  
https://github.com/Andrew0000/single_event/blob/main/lib/src/commonMain/kotlin/crocodile8/single_event/SingleEventStream.kt  
Typically in ViewModel:  
```
private val _events = SingleEventStream<Any>(viewModelScope)
val events = _events.asReadOnly()

fun onClick() {
    _events.push("Something")
}
```

Typically in a Composable:  
```
val lifecycleOwner = LocalLifecycleOwner.current
LaunchedEffect(Unit) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.events.collect {
            // Handle the events here
        }
    }
}
```
