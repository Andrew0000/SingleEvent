Implementation of the "One-Time Event" pattern for Android ond Kotlin Multiplatform.

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

History of the question and other solutions:  
https://github.com/Kotlin/kotlinx.coroutines/issues/2886#issuecomment-901188295  
https://medium.com/androiddevelopers/viewmodel-one-off-event-antipatterns-16a1da869b95  
