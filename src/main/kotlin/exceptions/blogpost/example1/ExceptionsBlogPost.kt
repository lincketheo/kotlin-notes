package exceptions.blogpost.example1

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import java.io.IOException
import java.net.http.HttpTimeoutException
import kotlin.random.Random

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

        delay(1000)
        repository.refresh()
        delay(1000)
        retrofitClient.shouldThrowError = true
        repository.refresh()
        delay(1000)
        retrofitClient.shouldThrowError = false
        localStorage.shouldThrowError = true
        repository.refresh()
        delay(1000)
    }
}

class CachingRepository(
    private val retrofitClient: FakeNetworkDataSource,
    private val localStorage: FakeLocalStorage,
) {
    val sourceOfTruthData = localStorage.localStorage

    fun refresh() {
        try {
            val data = retrofitClient.getData()
            localStorage.storeData(data)
        } catch (e: HttpTimeoutException) {
            println("Http response timed out, cache is unchanged - probably show a pop up to the user")
        } catch (e: IOException) {
            println("Local storage failed - probably show a pop up to the user")
        }
    }
}

class FakeNetworkDataSource {
    var shouldThrowError = false
    fun getData(): MyData {
        return attemptNetworkCall()
    }

    /**
     * This would likely be from another class (like a retrofit client class)
     * Notably, this isn't the class that would be throwing an exception in the
     * first place - the [HttpTimeoutException] would be thrown elsewhere.
     */
    private fun attemptNetworkCall(): MyData {
        if(shouldThrowError){
            throw HttpTimeoutException("Network exception timed out")
        }
        return MyData.random()
    }
}

class FakeLocalStorage {
    var shouldThrowError = false
    private val _localStorage = MutableStateFlow<MyData?>(null)
    val localStorage = _localStorage.asStateFlow()
    fun storeData(data: MyData){
        attemptLocalStorageCall(data)
    }

    private fun attemptLocalStorageCall(data: MyData) {
        if(shouldThrowError) {
            throw IOException("Local storage failed")
        }
        _localStorage.value = data
    }
}
