package com.kurome.app.internal.packet.system

import botsetting.BotCommon
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
}

@Serializable
data class SignRequest(
    val uin: Long,
    val cmd: String,
    val buffer: String,
    val guid: String,
    val seq: Int,
    val qua: String
)

@Serializable
data class SignResponse(
    val data: SignData
)


@Serializable
data class SignData(
    val sign: String,
    val token: String,
    val extra: String
)

fun getSecSign(botCommon: BotCommon, cmd: String, buffer: String): SignResponse? {
   return runBlocking {
        val body =
            SignRequest(botCommon.keystore.uin, cmd, buffer, botCommon.keystore.guid, 12345, botCommon.appinfo.qua)

         try {

            val response: SignResponse = client.post("http://127.0.0.1:5000/sign") {
                header("Content-Type", "application/json")
                setBody(body)
            }.body()
            response

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

