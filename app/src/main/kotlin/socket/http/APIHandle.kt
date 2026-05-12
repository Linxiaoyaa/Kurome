package socket.http

import botsetting.BotManager
import internal.service.system.wtLogin
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
                post("/api/login") {
                    try {
                        val req = call.receive<LoginRequest>()
                        val uin = req.uin
                        val guid = req.guid
                        val bot = BotManager.addAccount(uin, req.password, guid.hexToByteArray())
                        GlobalScope.launch(Dispatchers.IO) {
                            bot.wtLogin()
                        }
                        call.respond(APIResponse(
                            status = 200,
                            message = "Login task initialized",
                            data = "Bot for $uin is now processing..."
                        ))
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest, APIResponse(
                            status = 400,
                            message = e.message ?: "Unknown error",
                            data = null
                        ))
                    }
                }
                get("/api/bot/status/{uin}") {
                    val uin = call.parameters["uin"]?.toLongOrNull()
                    val bot = uin?.let { BotManager.getBot(it) }
                    if (bot != null) {
                        call.respond(APIResponse(
                            status = 200,
                            message = "Success",
                            data = LoginResponse(
                                loginCode = if (bot.success) 0 else -1,
                                loginMsg = bot.keystore.ErrorMessage,
                                loginTitle = bot.keystore.ErrorTitle
                            )
                        ))
                    } else {
                        call.respond(HttpStatusCode.NotFound, APIResponse<String>(404, "Bot not found", null))
                    }
                }
            }
        }.start(wait = false)
    }


}