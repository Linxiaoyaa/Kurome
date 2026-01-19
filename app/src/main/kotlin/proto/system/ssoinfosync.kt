@file:OptIn(ExperimentalSerializationApi::class)
package proto.system

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class SsoC2CMsgCookie(
    @ProtoNumber(1) val c2cLastMsgTime: ULong = 0uL
)

@Serializable
data class SsoC2CSyncInfo(
    @ProtoNumber(1) val c2cMsgCookie: SsoC2CMsgCookie = SsoC2CMsgCookie(),
    @ProtoNumber(2) val c2cLastMsgTime: ULong = 0uL,
    @ProtoNumber(3) val lastC2CMsgCookie: SsoC2CMsgCookie = SsoC2CMsgCookie()
)

@Serializable
data class DeviceInfo(
    @ProtoNumber(1) val devName: String = "",
    @ProtoNumber(2) val devType: String = "",
    @ProtoNumber(3) val osVer: String = "",
    @ProtoNumber(4) val brand: String = "",
    @ProtoNumber(5) val vendorOsName: String = ""
)

@Serializable
data class OnlineBusinessInfo(
    @ProtoNumber(1) val notifySwitch: UInt = 0u,
    @ProtoNumber(2) val bindUinNotifySwitch: UInt = 0u
)

@Serializable
data class RegisterInfo(
    @ProtoNumber(1) val guid: String = "",
    @ProtoNumber(2) val kickPC: UInt = 0u,
    @ProtoNumber(3) val buildVer: String = "",
    @ProtoNumber(4) val isFirstRegisterProxyOnline: UInt = 0u,
    @ProtoNumber(5) val localeId: UInt = 0u,
    @ProtoNumber(6) val deviceInfo: DeviceInfo = DeviceInfo(),
    @ProtoNumber(7) val setMute: UInt = 0u,
    @ProtoNumber(8) val registerVendorType: UInt = 0u,
    @ProtoNumber(9) val regType: UInt = 0u,
    @ProtoNumber(10) val businessInfo: OnlineBusinessInfo = OnlineBusinessInfo(),
    @ProtoNumber(11) val batteryStatus: UInt = 0u,
    @ProtoNumber(12) val field12: Int? = 0
)

@Serializable
data class NormalConfig(
    @ProtoNumber(1) val intCfg: Map<UInt, Int> = emptyMap()
)

@Serializable
data class CurAppState(
    @ProtoNumber(1) val isDelayRequest: UInt = 0u,
    @ProtoNumber(2) val appStatus: UInt = 0u,
    @ProtoNumber(3) val silenceStatus: UInt = 0u
)

@Serializable
data class SsoInfoSyncRequest(
    @ProtoNumber(1) val syncFlag: UInt = 735u,
    @ProtoNumber(2) val reqRandom: UInt = 0u,
    @ProtoNumber(4) val curActiveStatus: UInt = 0u,
    @ProtoNumber(5) val groupLastMsgTime: ULong = 0uL,
    @ProtoNumber(6) val c2cSyncInfo: SsoC2CSyncInfo = SsoC2CSyncInfo(),
    @ProtoNumber(8) val normalConfig: NormalConfig = NormalConfig(),
    @ProtoNumber(9) val registerInfo: RegisterInfo = RegisterInfo(),
    //@ProtoNumber(10) val unknown: Map<UInt, UInt> = emptyMap(),
    @ProtoNumber(11) val appState: CurAppState = CurAppState()
)

@Serializable
data class RegisterResponse(
    @ProtoNumber(2) val msg: String = ""
)

@Serializable
data class SsoSyncInfoResponse(
    @ProtoNumber(7) val registerResponse: RegisterResponse? = null
)