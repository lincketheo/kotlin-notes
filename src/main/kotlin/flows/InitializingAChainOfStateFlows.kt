package flows

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

val parentCoroutineScope = CoroutineScope(Dispatchers.Main)

val driverFlow = MutableStateFlow<Int>(0)

suspend fun backgroundWorkDrivingFlow() {
    delay(100)
    driverFlow.emit(1)
    delay(100)
    driverFlow.emit(2)
}

suspend fun foregroundWorkListeningToDriver() {
    driverFlow
        .onEach { println("Foreground received $it from background") }
        .map { it + 5 }

}
