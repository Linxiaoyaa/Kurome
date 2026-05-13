import internal.packet.system.BotNetworkManager
import internal.service.runtime.AppScope
import io.github.oshai.kotlinlogging.KotlinLogging
import socket.http.APIHandle


private val logger = KotlinLogging.logger {}

fun main()  {
    logger.info { "Welcome to the Kurome!" }
    APIHandle.start(8888)
    Thread.currentThread().join()
    BotNetworkManager.shutdown()
    AppScope.shutdown()
}

