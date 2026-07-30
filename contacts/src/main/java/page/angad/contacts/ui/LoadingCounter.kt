package page.angad.contacts.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class LoadingCounter(val scope: CoroutineScope) {
    private val _count = MutableStateFlow(0)

    fun inc() {
        _count.update { it + 1 }
    }

    fun dec() {
        _count.update { it - 1 }
    }

    @Composable
    fun state(): State<Int> {
        return _count.collectAsState()
    }
}

@Suppress("ComposableNaming")
fun Job.attach(counter: LoadingCounter): Job {
    counter.inc()
    invokeOnCompletion {
        counter.scope.launch {
            withContext(Dispatchers.Main) {
                delay(200.milliseconds)
                counter.dec()
            }
        }
    }

    return this
}