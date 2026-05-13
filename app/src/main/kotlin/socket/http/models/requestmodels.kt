package socket.http.models

import kotlinx.serialization.Serializable

@Serializable


data class APIResponse<T>(
    val status: Int,
    val message: String,
    val data: T? = null
)
@Serializable
data class SystemInfo(
    val os: String,
    val memory_used_mb: Long,
    val bot_count: Int
)


