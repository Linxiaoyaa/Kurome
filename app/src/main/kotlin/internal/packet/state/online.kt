package internal.packet.state
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import botsetting.BotCommon
import internal.packet.system.bufferHead
import internal.packet.system.buildOnlineHead
import proto.system.SsoInfoSyncRequest
import kotlin.random.Random
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.decodeFromByteArray
import proto.system.*
@OptIn(ExperimentalSerializationApi::class)
val myProtoBuf = ProtoBuf {
    encodeDefaults = true

}
@OptIn(ExperimentalSerializationApi::class)
fun online(botCommon: BotCommon): Boolean{
    val bin = buildSsoInfoSyncRequest(botCommon)

    val newSsoInfoSyncRequest = buildOnlineHead(botCommon,bin , "trpc.msg.register_proxy.RegisterProxy.SsoInfoSync")
    println("online:${newSsoInfoSyncRequest.toHexString()}")
    val ssoSyncInfoResponseBuffer = botCommon.client.send(newSsoInfoSyncRequest)
    println(ssoSyncInfoResponseBuffer?.let { "resp:${it.toHexString()}" })
    if (ssoSyncInfoResponseBuffer!= null){
        val response = myProtoBuf.decodeFromByteArray<SsoSyncInfoResponse>(ssoSyncInfoResponseBuffer)
        response.registerResponse?.let {
            if(it.msg == "register success"){
                return true
            }
        }
    }


    return false
}

@OptIn(ExperimentalSerializationApi::class)
fun buildSsoInfoSyncRequest(bot: BotCommon): ByteArray {
    val packet = SsoInfoSyncRequest(
        syncFlag = 1759u,
        reqRandom = Random.nextInt().toUInt(),
        curActiveStatus = 2u,
        groupLastMsgTime = 0uL,

        c2cSyncInfo = SsoC2CSyncInfo(
            c2cMsgCookie = SsoC2CMsgCookie(c2cLastMsgTime = 0uL),
            c2cLastMsgTime = 0uL,
            lastC2CMsgCookie = SsoC2CMsgCookie(c2cLastMsgTime = 0uL)
        ),

        normalConfig = NormalConfig(
            intCfg = mapOf(46u to 0, 283u to 0)
        ),

        registerInfo = RegisterInfo(
            guid = bot.keystore.guid,
            kickPC = 0u,
            buildVer = "27050",
            isFirstRegisterProxyOnline = 0u,
            localeId = 2052u,
            deviceInfo = DeviceInfo(
                devName = bot.keystore.deviceName,
                devType = "Android",
                osVer = "16",
                brand = "Kurome",
                vendorOsName = "114514"
            ),
            setMute = 0u,
            registerVendorType = 1u,
            regType = 1u,
            businessInfo = OnlineBusinessInfo(
                notifySwitch = 1u,
                bindUinNotifySwitch = 1u
            ),
            batteryStatus = 0u
        ),


        appState = CurAppState(
            isDelayRequest = 0u,
            appStatus = 1u,
            silenceStatus = 0u
        )
    )

    return myProtoBuf.encodeToByteArray(packet)
}