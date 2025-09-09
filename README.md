Implementation of the "One-Time Event" pattern for Android and Kotlin Multiplatform.  
Typical usage is ViewModel with Jetpack Compose / Compose Multiplatform.  

# History of the question and other solutions  

### LiveData-based solutions  
Outdated as of 2025.

### Kotlin Channels   
Solutions based on Channels have a limitation: they must operate on Main / Main.immediate scheduler to be reliable.  
More inforamation:

[ViewModel: One-off event antipatterns](https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95)    
> A Channel doesn’t guarantee the delivery and processing of the events. Therefore, events can be lost, leaving the UI in an inconsistent state.  

[The idea about Main.immediate](https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295)  
> The most trivial solution is to rely on the specifics of how view lifecycle works in Android and on Dispatchers.Main.immediate.  

[ViewModel: Events as State are an Antipattern](https://proandroiddev.com/viewmodel-events-as-state-are-an-antipattern-35ff4fbc6fb6)  
> Exposing events from the VM does not mean that the VM is not the source of truth.

### State + notify about consumption  
It works but requires additional code which can be tedious.  
[Google's approach (explicitly consume events)](https://developer.android.com/topic/architecture/ui-layer/events#consuming-trigger-updates)  
> UI needs to notify the ViewModel to trigger another state update when the message has been shown on the screen.  

### SingleEventStream (this soultion, middle ground)  
Key features:
- It is guaranteed that a pushed even will not be lost.
- Many events may be queued until someone collects them.
- New event can be pushed from any thread / dispatcher.
- Collection may be happening from any thread / dispatcher.
- Order of events is not guaranteed if pushed from multiple threads / dispatchers.

Limitations:
- Only 1 collector at a time is supported.

How and why it works:  
Events are stored in the internal queue. Any access to the queue happens under the Mutex. Any event that was not consumed waits in the queue. Collector automatically and atomically consumes the queue.
It's partially similar to what Google recommends (notify about the consumption), but automatic.

# Usage example  
The library is not published. Basically, it's just 1 class, so you can copy it to your project.  
https://github.com/Andrew0000/SingleEvent/blob/main/lib/src/commonMain/kotlin/crocodile8/single_event/SingleEventStream.kt  
Tests:  
https://github.com/Andrew0000/SingleEvent/blob/main/lib/src/commonTest/kotlin/crocodile8/single_event/SingleEventStreamTest.kt  

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
