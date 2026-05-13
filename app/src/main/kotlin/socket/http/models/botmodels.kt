package socket.http.models

import kotlinx.serialization.Serializable


@Serializable
data class AccountInfo(val uin: Long, val password: String, val guid: String? = null)

@Serializable
data class BatchAddRequest(val accounts: List<AccountInfo>)

@Serializable
data class BotStatusSummary(
    val uin: Long,
    val success: Boolean,
    val code: Int,
    val title: String,
    val msg: String,
    val ticketUrl: String? = null
)