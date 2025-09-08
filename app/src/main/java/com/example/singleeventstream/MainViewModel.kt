package com.example.singleeventstream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import crocodile8.single_event.SingleEventStream
import crocodile8.single_event.asReadOnly
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
class MainViewModel : ViewModel() {

    private val _events = SingleEventStream<Any>(viewModelScope)
    val events = _events.asReadOnly()

    private val clickCounter = AtomicInt(0)

    fun onClick() {
        _events.push("Something: ${clickCounter.incrementAndFetch()}")
    }
}