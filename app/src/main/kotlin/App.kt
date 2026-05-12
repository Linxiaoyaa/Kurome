import botsetting.BotManager
import internal.packet.system.BotNetworkManager
import io.github.oshai.kotlinlogging.KotlinLogging
import socket.http.APIHandle


private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Welcome to the Kurome!" }
    BotManager.loadAllSavedAccounts()
    APIHandle.start(8888)
    Thread.currentThread().join()
    BotNetworkManager.shutdown()
}

