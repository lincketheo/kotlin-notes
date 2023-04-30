package exceptions.blogpost.example4

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.IOException
import java.net.http.HttpTimeoutException
import kotlin.random.Random
import kotlin.reflect.KClass

data class MyData(
    val num: Int,
    val name: String,
) {
    companion object {
        fun random(): MyData {
            return MyData(Random.nextInt(), "Hello World")
        }
    }
}

fun errorHandlingExampleMain() {
    val retrofitClient = FakeNetworkDataSource()
    val localStorage = FakeLocalStorage()
    val repository = CachingRepository(
        retrofitClient,
        localStorage,
    )

    runBlocking {
        val listeningScope = CoroutineScope(CoroutineName("Repository") + Dispatchers.Default)
        listeningScope.launch {
            repository.sourceOfTruthData
                .onEach {
                    println("Current Source of Truth is: $it")
                }
                .collect()
        }

        fun showSnackBarIfFailed(block: () -> Unit) {
            try {
                block()
            } catch (e: AppDataSourceException) {
                println("Error: I'm showing a snack bar! Problem: ${e.message}")
            }
        }

        delay(1000)
        showSnackBarIfFailed {
            repository.refresh()
        }
        delay(1000)
        retrofitClient.shouldThrowError = true
        showSnackBarIfFailed {
            repository.refresh()
        }
        delay(1000)
        retrofitClient.shouldThrowError = false
        localStorage.shouldThrowError = true
        showSnackBarIfFailed {
            repository.refresh()
        }
        delay(1000)
    }
}

class CachingRepository(
    private val retrofitClient: FakeNetworkDataSource,
    private val localStorage: FakeLocalStorage,
) {
    val sourceOfTruthData = localStorage.localStorage

    fun refresh() {
        val data = retrofitClient.getData()
        localStorage.storeData(data)
    }
}

class AppDataSourceException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

inline fun <T> attemptDataSourceOperation(
    catchExceptionTypes: Set<KClass<out Exception>>,
    onFailExposedMessage: String,
    block: () -> T
): T {
    try {
        return block()
    } catch (e: Exception) {
        catchExceptionTypes.find { it.isInstance(e) }?.let {
            throw AppDataSourceException(
                message = onFailExposedMessage,
                cause = e,
            )
        }
        throw e
    }
}


class FakeNetworkDataSource {
    var shouldThrowError = false
    fun getData(): MyData {
        return attemptDataSourceOperation(
            catchExceptionTypes = setOf(HttpTimeoutException::class),
            onFailExposedMessage = "Fetching data from network failed",
        ) {
            attemptNetworkCall()
        }
    }

    /**
     * This would likely be from another class (like a retrofit client class)
     * Notably, this isn't the class that would be throwing an exception in the
     * first place - the [HttpTimeoutException] would be thrown elsewhere.
     */
    private fun attemptNetworkCall(): MyData {
        if (shouldThrowError) {
            throw HttpTimeoutException("Network exception timed out")
        }
        return MyData.random()
    }
}

class FakeLocalStorage {
    var shouldThrowError = false
    private val _localStorage = MutableStateFlow<MyData?>(null)
    val localStorage = _localStorage.asStateFlow()
    fun storeData(data: MyData) {
        return attemptDataSourceOperation(
            catchExceptionTypes = setOf(IOException::class),
            onFailExposedMessage = "Storing data failed",
        ) {
            attemptLocalStorageCall(data)
        }
    }

    private fun attemptLocalStorageCall(data: MyData) {
        if (shouldThrowError) {
            throw IOException("Local storage failed")
        }
        _localStorage.value = data
    }
}
