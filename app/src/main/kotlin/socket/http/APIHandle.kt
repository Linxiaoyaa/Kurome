package socket.http
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi
import socket.http.routes.accountRoutes
import socket.http.routes.systemRoutes

private val logger = KotlinLogging.logger {}
object APIHandle {
    @OptIn(DelicateCoroutinesApi::class)
    fun start(port: Int = 8888) {
        logger.info { "Listening on port $port" }
        embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                systemRoutes()
                accountRoutes()
            }
        }.start(wait = false)
    }
}