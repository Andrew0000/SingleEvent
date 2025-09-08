package com.example.singleeventstream

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.singleeventstream.ui.theme.SingleEventStreamTheme

@Composable
fun MainScreen(name: String, modifier: Modifier = Modifier) {
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SingleEventStreamTheme {
        MainScreen("Android")
    }
}
