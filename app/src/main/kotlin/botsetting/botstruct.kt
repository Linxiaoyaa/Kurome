package botsetting

import com.kurome.app.socket.BotClient
import com.kurome.app.utils.crypto.ecdh.generateEcdhV2
import com.kurome.app.utils.getRandomBytes
import com.kurome.app.utils.getRandomString
import kotlinx.serialization.Serializable

@Serializable
data class WtLoginSdkInfo(
    val sdkBuildTime: UInt = 1757058014u,
    val sdkVersion: String = "6.0.0.2589",
    val miscBitMap: UInt = 150470524u,
    val subSigMap: UInt = 66560u,
    val mainSigMap: UInt = 169297956u
)

@Serializable
data class BotAppinfo(
    val os: String = "Android",
    val vendors: String = "android",
    val kernel: String = "linux",
    val qua: String = "V1_AND_SQ_9.2.20_11650_YYB_D",
    val currentVersion: String = "9.2.20.777b5929",
    val ptVersion: String = "9.2.20",
    val ssoVersion: Int = 22,
    val packageName: String = "com.tencent.mobileqq",
    val apkSignatureMD5: String = "Y29tLnRlbmNlbnQucXE=",
    val sdkInfo: WtLoginSdkInfo = WtLoginSdkInfo(),
    val appId: UInt = 16u,
    val subAppId: Long = 537315825L,
    val appClientVersion: UInt = 0u
)

@Suppress("ArrayInDataClass")
data class BotKeystore(
    var guid: String = getRandomBytes(16).toHexString(),
    var uin: Long = 0L,
    val qimei: String = "b9a1be24277f73daef6d88ca100016d1730c",
    val androidId: String = getRandomString(16),
    val deviceName: String = "Kurome_" + getRandomString(4),
    var password: String = "",
    var passwordKey: ByteArray = byteArrayOf(),
    var password2Key: ByteArray = byteArrayOf(),
    var ECDH: BotECDH = generateEcdhV2(),
    var mac: String = "02:00:00:00:00:00",
    var Iframe: BotIframe = BotIframe(),
    var WLoginSigs: WLoginSigs = WLoginSigs(),
    var State: State = State(),
    var ErrorTitle : String = "",
    var ErrorMessage: String = ""
)

data class BotCommon(
    var keystore: BotKeystore,
    var appinfo: BotAppinfo,
    var client: BotClient = BotClient("msfwifi.3g.qq.com", 8080)
)

data class BotECDH(
    var publicKey: ByteArray = byteArrayOf(),
    var shareKey: ByteArray = byteArrayOf()
)

data class BotIframe(
    var url: String = "",
    var ticket: String = "",
    var sig: String = "",
    var randStr: String = ""
)

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


)

data class State(
    var Tlv104: ByteArray = byteArrayOf(),
    var Tlv547: ByteArray = byteArrayOf(),
    var Tlv174: ByteArray = byteArrayOf()
)