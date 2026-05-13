@file:UseSerializers(ByteArrayHexSerializer::class)
package botsetting

import io.github.oshai.kotlinlogging.KotlinLogging
import socket.tcp.BotClient
import utils.crypto.ecdh.generateEcdhV2
import utils.getRandomBytes
import utils.getRandomString
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.KSerializer
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.HexFormat
import java.util.concurrent.atomic.AtomicInteger


object ByteArrayHexSerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ByteArrayHex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(HexFormat.of().formatHex(value))
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        return HexFormat.of().parseHex(decoder.decodeString())
    }
}

@Serializable
data class WtLoginSdkInfo(
    val sdkBuildTime: UInt = 1757058014u,
    val sdkVersion: String = "6.0.0.2591",
    val miscBitMap: UInt = 150470524u,
    val subSigMap: UInt = 66560u,
    val mainSigMap: UInt = 16724722u
)
@Serializable
data class BotLoginInfo  (
    var code :Int = 0,
    var title :String = "",
    var msg :String = "",
    var data: String ?=null
)
@Serializable
data class BotAppinfo(
    val os: String = "Android",
    val vendors: String = "android",
    val kernel: String = "linux",
    val qua: String = "V1_AND_SQ_9.2.85_13860_YYB_D",
    val currentVersion: String = "9.2.85.4747ba3e",
    val ptVersion: String = "9.2.85",
    val ssoVersion: Int = 22,
    val packageName: String = "com.tencent.mobileqq",
    val apkSignatureMD5: String = "Y29tLnRlbmNlbnQucXE=",
    val sdkInfo: WtLoginSdkInfo = WtLoginSdkInfo(),
    val appId: UInt = 16u,
    val subAppId: Long = 537351341L,
    val appClientVersion: UInt = 0u
)

@Suppress("ArrayInDataClass")@Serializable
data class BotKeystore(
    var guid: String = getRandomBytes(16).toHexString(),
    var uin: Long = 0L,
    val qimei: String = "b9a1be24277f73daef6d88ca100016d1730c",
    val androidId: String = getRandomString(16),
    val deviceName: String = "Kurome_" + getRandomString(6),
    var password: String = "",
    var passwordKey: ByteArray = byteArrayOf(),
    var password2Key: ByteArray = byteArrayOf(),
    var ECDH: BotECDH = generateEcdhV2(),
    var mac: String = "02:00:00:00:00:00",
    var Iframe: BotIframe = BotIframe(),
    var WLoginSigs: WLoginSigs = WLoginSigs(),
    var State: State = State(),
    var ErrorTitle : String = "",
    var ErrorMessage: String = "",
    var MsgCookies:ByteArray = getRandomBytes(4),
    var seq: Int = (100000 until 999999).random()

){
    private val ssoSeqCounter: AtomicInteger by lazy { AtomicInteger(seq) }
    val SsoSeq: Int
        get() = ssoSeqCounter.getAndIncrement().also {
            seq = it + 1
        }
}
@Serializable
data class BotCommon(
    var keystore: BotKeystore,
    var appinfo: BotAppinfo,
    var loginInfo: BotLoginInfo,
    var success: Boolean = false,
){
    @Transient
    lateinit var client: BotClient
    @Transient
    val log = KotlinLogging.logger("[${keystore.uin}]")
    fun initClient(host: String = "msfwifi.3g.qq.com", port: Int = 8080) {
        this.client = BotClient(host, port, this)
    }
}
@Serializable
data class BotECDH(
    var publicKey: ByteArray = byteArrayOf(),
    var shareKey: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BotECDH

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!shareKey.contentEquals(other.shareKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + shareKey.contentHashCode()
        return result
    }
}

@Serializable
data class BotIframe(
    var url: String = "",
    var ticket: String = "",
    var sig: String = "",
    var randStr: String = ""
)
@Serializable
data class WLoginSigs(
    var A2: ByteArray = byteArrayOf(),
    var A2Key: ByteArray = byteArrayOf(),
    var D2: ByteArray = byteArrayOf(),
    var D2Key: ByteArray = byteArrayOf(),
    var A1: ByteArray = byteArrayOf(),
    var A1Key: ByteArray = byteArrayOf(),
    var NoPicSig: ByteArray = byteArrayOf(),
    var RandomKey: ByteArray = getRandomBytes(16),
    var TGTGTKey: ByteArray = byteArrayOf(),
    var Ksid: ByteArray = byteArrayOf(),
    var SKey: ByteArray = byteArrayOf(),
    var WtSessionTicket: ByteArray = byteArrayOf(),
    var WtSessionTicketKey: ByteArray = byteArrayOf(),
    var SuperKey: ByteArray = byteArrayOf(),
    var StKey: ByteArray = byteArrayOf(),
    var St: ByteArray = byteArrayOf(),
    var StWeb: ByteArray = byteArrayOf()


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WLoginSigs

        if (!A2.contentEquals(other.A2)) return false
        if (!A2Key.contentEquals(other.A2Key)) return false
        if (!D2.contentEquals(other.D2)) return false
        if (!D2Key.contentEquals(other.D2Key)) return false
        if (!A1.contentEquals(other.A1)) return false
        if (!A1Key.contentEquals(other.A1Key)) return false
        if (!NoPicSig.contentEquals(other.NoPicSig)) return false
        if (!RandomKey.contentEquals(other.RandomKey)) return false
        if (!TGTGTKey.contentEquals(other.TGTGTKey)) return false
        if (!Ksid.contentEquals(other.Ksid)) return false
        if (!SKey.contentEquals(other.SKey)) return false
        if (!WtSessionTicket.contentEquals(other.WtSessionTicket)) return false
        if (!WtSessionTicketKey.contentEquals(other.WtSessionTicketKey)) return false
        if (!SuperKey.contentEquals(other.SuperKey)) return false
        if (!StKey.contentEquals(other.StKey)) return false
        if (!St.contentEquals(other.St)) return false
        if (!StWeb.contentEquals(other.StWeb)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = A2.contentHashCode()
        result = 31 * result + A2Key.contentHashCode()
        result = 31 * result + D2.contentHashCode()
        result = 31 * result + D2Key.contentHashCode()
        result = 31 * result + A1.contentHashCode()
        result = 31 * result + A1Key.contentHashCode()
        result = 31 * result + NoPicSig.contentHashCode()
        result = 31 * result + RandomKey.contentHashCode()
        result = 31 * result + TGTGTKey.contentHashCode()
        result = 31 * result + Ksid.contentHashCode()
        result = 31 * result + SKey.contentHashCode()
        result = 31 * result + WtSessionTicket.contentHashCode()
        result = 31 * result + WtSessionTicketKey.contentHashCode()
        result = 31 * result + SuperKey.contentHashCode()
        result = 31 * result + StKey.contentHashCode()
        result = 31 * result + St.contentHashCode()
        result = 31 * result + StWeb.contentHashCode()
        return result
    }
}

@Serializable
data class State(
    var Tlv104: ByteArray = byteArrayOf(),
    var Tlv547: ByteArray = byteArrayOf(),
    var Tlv174: ByteArray = byteArrayOf(),
    var SuccessTlv : ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (!Tlv104.contentEquals(other.Tlv104)) return false
        if (!Tlv547.contentEquals(other.Tlv547)) return false
        if (!Tlv174.contentEquals(other.Tlv174)) return false
        if (!SuccessTlv.contentEquals(other.SuccessTlv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Tlv104.contentHashCode()
        result = 31 * result + Tlv547.contentHashCode()
        result = 31 * result + Tlv174.contentHashCode()
        result = 31 * result + SuccessTlv.contentHashCode()
        return result
    }
}