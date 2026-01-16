package botsetting
import com.kurome.app.utils.*
import com.kurome.app.utils.crypto.ecdh.generateEcdhV2
import kotlinx.serialization.Serializable
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi
import kotlin.random.Random

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
    val guid: String = getRandomBytes(16).toHexString(),
    val uin : Long = 0L,
    val qimei : String = "b9a1be24277f73daef6d88ca100016d1730c",
    val androidId : String = getRandomString(8),
    val deviceName : String = "Kurome_"+getRandomString(4),
    var password : String = "",
    var passwordKey : ByteArray = byteArrayOf(),
    var password2Key : ByteArray= byteArrayOf(),
    var TGTGTKey : ByteArray= byteArrayOf(),
    var ECDH : BotECDH = generateEcdhV2(),
    var randomKey: ByteArray = getRandomBytes(16),
    var mac : String = "02:00:00:00:00:00"
)

data class BotCommon (
    var keystore: BotKeystore ,
    var appinfo: BotAppinfo
)
data class BotECDH(
    var publicKey: ByteArray = byteArrayOf(),
    var shareKey: ByteArray = byteArrayOf()
)