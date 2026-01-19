package internal.service.store

import botsetting.BotCommon
import botsetting.WLoginSigs
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.HexFormat

object SessionManager {
    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true // 即使是空的 ByteArray 也会保存为 ""
    }

    private val sessionDir = File("sessions").apply { if (!exists()) mkdirs() }

    /**
     * 【仅保存 WLoginSigs】
     * 这样生成的 JSON 文件只包含 A2, D2, SKey 等票据信息
     */
    fun saveSigs(bot: BotCommon) {
        val uin = bot.keystore.uin
        if (uin == 0L) return

        val file = File(sessionDir, "$uin.json")

        // 核心：只序列化 WLoginSigs 属性
        val jsonString = jsonConfig.encodeToString(bot.keystore.WLoginSigs)

        file.writeText(jsonString)
        println("Account $uin Sigs saved (Hex format)")
    }

    /**
     * 【仅加载 WLoginSigs】
     * 读取 JSON 并覆盖到传入的 bot 实例中
     */
    fun loadSigsIntoBot(uin: Long, bot: BotCommon): Boolean {
        val file = File(sessionDir, "$uin.json")
        if (!file.exists()) return false

        return try {
            val jsonString = file.readText()
            // 1. 解析出保存的 Sigs 对象
            val savedSigs = jsonConfig.decodeFromString<WLoginSigs>(jsonString)

            // 2. 将票据覆盖到当前 bot 的 keystore 中
            bot.keystore.WLoginSigs = savedSigs
            println("Account $uin Sigs loaded successfully")
            true
        } catch (e: Exception) {
            println("Load Sigs for $uin failed: ${e.message}")
            false
        }
    }
}

/**
 * 十六进制序列化器
 */
object ByteArrayHexSerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ByteArrayHex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        // ByteArray -> Hex String
        encoder.encodeString(HexFormat.of().formatHex(value))
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        // Hex String -> ByteArray
        val hexString = decoder.decodeString()
        return HexFormat.of().parseHex(hexString)
    }
}