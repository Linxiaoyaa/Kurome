package socket.http.routes

import botsetting.BotManager
import io.ktor.server.routing.*
import io.ktor.server.response.*

import socket.http.models.SystemInfo
import socket.http.models.APIResponse

fun Route.systemRoutes() {
    route("/api/system") {
        get("/info") {
            val runtime = Runtime.getRuntime()
            val memUsed = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val info = SystemInfo(
                os = System.getProperty("os.name") ?: "Unknown",
                memory_used_mb = memUsed,
                bot_count = BotManager.getCount()
            )
            call.respond(
                APIResponse(
                    status = 200,
                    message = "success",
                    data = info
                )
            )
        }
    }

}