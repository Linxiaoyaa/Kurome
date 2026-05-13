package socket.http.routes
import botsetting.BotManager
import internal.service.runtime.AppScope
import internal.service.system.wtLogin
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Routing
import io.ktor.server.response.*
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import socket.http.models.APIResponse
import socket.http.models.BatchAddRequest
import socket.http.models.BotStatusSummary

private val logger = KotlinLogging.logger {}
fun Routing.accountRoutes() {
    route("/api/bot") {
        post("/addAccount") {
            try {
                val req = call.receive<BatchAddRequest>()
                req.accounts.forEach { acc ->
                    val guid = acc.guid?.hexToByteArray() ?: utils.getRandomBytes(16)
                    BotManager.addAccount(acc.uin, acc.password, guid)
                }
                call.respond(
                    APIResponse(
                        200,
                        "成功添加 ${req.accounts.size} 个账号",
                        null
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "$e.message" }
                call.respond(APIResponse(400, "bad request", null))
            }
        }
        post("login") {
            try {
                val req = call.receive<List<Long>>()
                val botsToLogin = req.mapNotNull { BotManager.getBot(it) }
                AppScope.botScope.launch(Dispatchers.Default) {
                    val semaphore = Semaphore(50)
                    botsToLogin.forEach { bot ->
                        launch {
                            semaphore.withPermit {
                                bot.wtLogin()
                            }
                        }
                    }
                }
                call.respond(
                    APIResponse(
                        200,
                        "任务已启动",
                        req.size
                    )
                )
            } catch (e: Exception) {
                logger.error(e) {
                    "$e.message"
                }
                call.respond(HttpStatusCode.BadRequest, APIResponse<String>(400, "请求格式错误", null))
            }

        }
        post("getStatus") {
            try {
                val req = call.receive<List<Long>>()
                val statusList = req.map { uin ->
                    val bot = BotManager.getBot(uin)
                    if (bot != null) {
                        BotStatusSummary(
                            uin = uin,
                            success = bot.success,
                            code = bot.loginInfo.code,
                            title = bot.loginInfo.title,
                            msg = bot.loginInfo.msg,
                            ticketUrl = if (bot.loginInfo.code == 2) bot.keystore.Iframe.url else null
                        )
                    } else {

                        BotStatusSummary(
                            uin = uin,
                            success = false,
                            code = -404,
                            title = "Not Found",
                            msg = "this is not registered yet"
                        )
                    }
                }
                call.respond(APIResponse(
                    status = 200,
                    message = "查询成功",
                    data = statusList
                ))
            } catch (e:Exception) {
                logger.error(e) {"$e.message"}
                call.respond(HttpStatusCode.BadRequest, APIResponse<String>(400, "请求格式错误", null))
            }
        }

    }

}