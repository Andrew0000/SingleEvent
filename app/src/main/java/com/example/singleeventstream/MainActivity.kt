package com.example.singleeventstream

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.singleeventstream.ui.theme.SingleEventStreamTheme
import crocodile8.single_event.SingleEventStream
import crocodile8.single_event.asReadOnly
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MutableStateFlow(1)
        setContent {
            SingleEventStreamTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w("test_", "onDestroy")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val viewModel = viewModel { MainViewModel() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect {
                Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    Column(
        modifier = modifier
    ) {
        Text(
            text = "Hello $name!",
        )

        Button(
            onClick = viewModel::onClick
        ) {
            Text(
                text = "Click me x1",
            )
        }

        Button(
            onClick = {
                viewModel.onClick()
                viewModel.onClick()
            }
        ) {
            Text(
                text = "Click me x2",
            )
        }
    }
}

@OptIn(ExperimentalAtomicApi::class)
class MainViewModel : ViewModel() {

    private val _events = SingleEventStream<Any>(viewModelScope)
    val events = _events.asReadOnly()

    private val clickCounter = AtomicInt(0)

    fun onClick() {
        _events.push("Something: ${clickCounter.incrementAndFetch()}")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SingleEventStreamTheme {
        Greeting("Android")
    }
}
