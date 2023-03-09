package coroutines.android

import kotlinx.coroutines.*
import kotlin.time.Duration

/**
 * This is not very pretty, but it's just for notes
 */
fun simulateExternalScope(): Unit = runBlocking { // run blocking starts a coroutineScope that blocks the (by deault) Main dispatcher until all its functions are done
    withTimeoutOrNull(5000) { // with timeout launches a coroutineScope that is canceled after a certain timeout
        refreshDevices() // simulating clicking the refresh devices button. See this function for details - it launches two coroutine scopes

        // Try uncommenting delays to simulate when the app crashs or navigation occurs
        delay(1500) // delay enough for the api to finish, but not enough for the cache to finish
        //delay(500) // delay not enough for the api to finish
        //delay(3000) // delay enough for everything to finish

        // Try uncommenting to simulate a user navigating away or the app crashing / ending
        navigateAwayFromViewModel()
        //coroutines.android.crashOrCloseApplication()
        delay(Duration.INFINITE)
    }
}


// The two scopes, technically, if coroutines.android.getExternalScope crashes, coroutines.android.getViewModelScope also crashes
// but not because coroutines.android.getViewModelScope is a child of coroutines.android.getExternalScope
val externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
val viewModelScope = CoroutineScope(Job() + Dispatchers.Default)

fun crashOrCloseApplication() {
    // In no particular order
    externalScope.cancel()
    viewModelScope.cancel()
}

fun navigateAwayFromViewModel() {
    viewModelScope.cancel()
}

suspend fun refreshDevices() {
    viewModelScope.launch { // launches a coroutineScope attached to the viewModel

        // When the api call completes, we want to display that data, then
        // cache in the background
        // This extra launch is redundant
        // Launches a new job - inherits the parent's coroutineScope (coroutines.android.getViewModelScope)
        val apiCall = launch {
            // This try catch pattern handles when apiCall.cancel() or coroutines.android.getViewModelScope.cancel() is called (remember, if a parent scope calls cancel, all child scopes also cancel)
            try {
                println("I am starting a call to fetch devices from the api")
                withContext(Dispatchers.IO) {
                    delay(1000)
                }
                println("I have completed getting devices from the api")
            } finally {
                println("I'm cleaning up resources from the api call")
            }
        }

        // this is what makes launch redundant, but you could imagine that
        // maybe you would set up the database while the api call is being made
        // or doing other stuff while waiting for the api call to complete
        // otherwise, you could just make apiCall not inside a job - this looks
        // more readable though.
        apiCall.join()

        println("I'm updating the ui with fresh devices")
        externalScope.launch { // This scope is not attached to coroutines.android.getViewModelScope, so if coroutines.android.getViewModelScope.cancel() is called, this job won't be canceled (unlike the above call to launch)
            try {
                println("Now I'm going to update the cache, but the ui can continue doing things if it wants")
                withContext(Dispatchers.IO) {
                    delay(1000)
                }
                println("I just completed caching devices")
            } finally {
                println("I'm cleaning up database resources")
            }
        }
        println("I'm exiting the coroutines.android.getViewModelScope")
    }
}