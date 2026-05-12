@file:OptIn(ExperimentalSerializationApi::class)
package proto.system

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber


@Serializable
data class SsoC2CSyncInfo(
    @ProtoNumber(1) val c2cMsgCookie: ByteArray = byteArrayOf(),
    @ProtoNumber(2) val c2cLastMsgTime: ULong = 0uL,
    @ProtoNumber(3) val lastC2CMsgCookie: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SsoC2CSyncInfo

        if (!c2cMsgCookie.contentEquals(other.c2cMsgCookie)) return false
        if (c2cLastMsgTime != other.c2cLastMsgTime) return false
        if (!lastC2CMsgCookie.contentEquals(other.lastC2CMsgCookie)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = c2cMsgCookie.contentHashCode()
        result = 31 * result + c2cLastMsgTime.hashCode()
        result = 31 * result + lastC2CMsgCookie.contentHashCode()
        return result
    }
}

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
    @ProtoNumber(1) val guid: ByteArray = byteArrayOf(),
    @ProtoNumber(2) val kickPC: UInt = 0u,
    @ProtoNumber(3) val buildVer: String = "35125",
    @ProtoNumber(4) val isFirstRegisterProxyOnline: UInt = 0u,
    @ProtoNumber(5) val localeId: UInt = 2052u,
    @ProtoNumber(6) val deviceInfo: DeviceInfo = DeviceInfo(),
    @ProtoNumber(7) val setMute: UInt = 0u,
    @ProtoNumber(8) val registerVendorType: UInt = 1u,
    @ProtoNumber(9) val regType: UInt = 1u,
    @ProtoNumber(10) val businessInfo: OnlineBusinessInfo = OnlineBusinessInfo(),
    @ProtoNumber(11) val field11: Int = 0,
    @ProtoNumber(14) val field14: ReservedField14 = ReservedField14(),
    @ProtoNumber(16) val batteryStatus: UInt = 0u,
    @ProtoNumber(17) val field17: Int = 0,
    @ProtoNumber(18) val field18: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterInfo

        if (field11 != other.field11) return false
        if (field17 != other.field17) return false
        if (field18 != other.field18) return false
        if (!guid.contentEquals(other.guid)) return false
        if (kickPC != other.kickPC) return false
        if (buildVer != other.buildVer) return false
        if (isFirstRegisterProxyOnline != other.isFirstRegisterProxyOnline) return false
        if (localeId != other.localeId) return false
        if (deviceInfo != other.deviceInfo) return false
        if (setMute != other.setMute) return false
        if (registerVendorType != other.registerVendorType) return false
        if (regType != other.regType) return false
        if (businessInfo != other.businessInfo) return false
        if (field14 != other.field14) return false
        if (batteryStatus != other.batteryStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field11
        result = 31 * result + field17
        result = 31 * result + field18
        result = 31 * result + guid.contentHashCode()
        result = 31 * result + kickPC.hashCode()
        result = 31 * result + buildVer.hashCode()
        result = 31 * result + isFirstRegisterProxyOnline.hashCode()
        result = 31 * result + localeId.hashCode()
        result = 31 * result + deviceInfo.hashCode()
        result = 31 * result + setMute.hashCode()
        result = 31 * result + registerVendorType.hashCode()
        result = 31 * result + regType.hashCode()
        result = 31 * result + businessInfo.hashCode()
        result = 31 * result + field14.hashCode()
        result = 31 * result + batteryStatus.hashCode()
        return result
    }
}
@Serializable
data class ReservedField14(
    @ProtoNumber(1) val v1: Int = 0,
    @ProtoNumber(2) val v2: Int = 0
)
@Serializable
data class NormalConfig(
    @ProtoNumber(1) val intCfg: Map<UInt, Int> = emptyMap()
)



@Serializable
data class SsoField10(
    @ProtoNumber(2) val field2: Int = 0, //{10.2}
    @ProtoNumber(4) val field4: SsoField10Inner = SsoField10Inner() //{10.4}
)

@Serializable
data class SsoField10Inner(
    @ProtoNumber(1) val field1: Int = 0 // {10.4.1}
)

@Serializable
data class SsoField11(
    @ProtoNumber(1) val field1: Int = 0, // {11.1}
    @ProtoNumber(2) val field2: Int = 1, // {11.2}
    @ProtoNumber(3) val field3: Int = 0  // {11.3}
)

@Serializable
data class SsoInfoSyncRequest(
    @ProtoNumber(1) val syncFlag: UInt = 1759u,
    @ProtoNumber(2) val reqRandom: UInt = 0u,
    @ProtoNumber(4) val curActiveStatus: UInt = 2u,
    @ProtoNumber(5) val groupLastMsgTime: ULong = 0uL,
    @ProtoNumber(6) val c2cSyncInfo: SsoC2CSyncInfo = SsoC2CSyncInfo(),
    @ProtoNumber(8) val normalConfig: NormalConfig = NormalConfig(),
    @ProtoNumber(9) val registerInfo: RegisterInfo = RegisterInfo(),
    @ProtoNumber(10) val field10: SsoField10 = SsoField10(),
    @ProtoNumber(11) val field11: SsoField11 = SsoField11()
)

@Serializable
data class RegisterResponse(
    @ProtoNumber(2) val msg: String = ""
)

@Serializable
data class SsoSyncInfoResponse(
    @ProtoNumber(7) val registerResponse: RegisterResponse? = null
)