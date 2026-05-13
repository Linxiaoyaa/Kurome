package internal.service.runtime

import kotlinx.coroutines.*

object AppScope {

    val botScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    fun shutdown() {
        botScope.cancel()
    }
}