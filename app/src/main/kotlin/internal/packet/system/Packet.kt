package internal.packet.system

import botsetting.BotCommon
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.serialization.protobuf.ProtoBuf
import proto.SsoClientReq
import proto.SsoClientReqPlain2
import utils.crypto.tea.TeaProvider
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray


data class Packet (
    val seq : Int,
    val body : ByteArray,
    val cmd : String,
    val errCode : Int,
    val errMsg : String,
    val msgCookies :ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        if (seq != other.seq) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seq
        result = 31 * result + body.contentHashCode()
        return result
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun buildSSOClientReq(signResponse: SignResponse): ByteArray {
    val protoBuf = ProtoBuf {
        encodeDefaults = true
    }
    val request = SsoClientReq(
        plain2 = SsoClientReqPlain2(
            sign = signResponse.data.sign.hexToByteArray(),
            token = signResponse.data.token.hexToByteArray(),
            extra = signResponse.data.extra.hexToByteArray()
        ),

        )
    return protoBuf.encodeToByteArray(request)
}

fun decodeHeader(bin: ByteArray, bot: BotCommon): Packet {
    var allBody: ByteArray
    val up = Buffer()
    val encryptType: Byte
    up.write(bin).apply {
        up.readInt()
        up.readInt()
        encryptType = up.readByte()
        up.readInt()
        val uinLen = up.readByte() - 4
        up.readByteArray(uinLen)
        allBody = up.readByteArray()
    }
    try {
        allBody = when (encryptType) {
            2.toByte() -> {
                TeaProvider.decrypt(allBody, ByteArray(16) { 0 })
            }

            1.toByte() -> {
                TeaProvider.decrypt(allBody, bot.keystore.WLoginSigs.D2Key)
            }

            else -> ByteArray(0)
        }!!
    } catch (e: Exception) {
        bot.log.error(e) { "Failed to decrypt packet: $encryptType" }
    }


    return readFuncBuffer(allBody,bot.log)
}

fun readFuncBuffer(bin: ByteArray,log: KLogger) :Packet{
    val up = Buffer()
    val seq : Int
    val errCode : Int
    val errMsg : String
    val cmd : String
    val msgCookies :ByteArray
    val header : ByteArray
    val buffer :ByteArray
    return try {
        up.write(bin).apply {
            up.readInt()
            seq = up.readInt()
            errCode = up.readInt()
            val errMsgLen = up.readInt() - 4
            errMsg = up.readByteArray(errMsgLen).decodeToString()
            val cmdLen = up.readInt() - 4
            cmd = up.readByteArray(cmdLen).decodeToString()
            up.readInt()
            msgCookies = up.readByteArray(4)
            up.readInt()
            val headLen = up.readInt() - 4
            header = up.readByteArray(headLen)
            val bufferLen = up.readInt() - 4
            buffer = up.readByteArray(bufferLen)
        }

        Packet(seq, buffer, cmd, errCode, errMsg, msgCookies)
    } catch (e : Exception) {
        log.error(e) { "Failed to read packet ${bin.toHexString()}" }
        Packet(0, byteArrayOf(), "error", -1, "invalid buffer", byteArrayOf())
    }
}