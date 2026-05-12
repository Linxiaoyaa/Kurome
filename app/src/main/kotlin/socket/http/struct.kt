package socket.http

import kotlinx.serialization.Serializable

@Serializable


data class APIResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null
)

@Serializable
data class LoginRequest(
    val uin: Long,
    val password: String,
    val guid: String
)

@Serializable
data class LoginResponse(
    val loginCode: Int,
    val loginMsg: String,
    val loginTitle: String,
)

