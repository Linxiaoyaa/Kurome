package internal.packet.system
import botsetting.BotCommon
import kotlinx.serialization.protobuf.ProtoBuf
import proto.SsoClientReq
import proto.SsoClientReqPlain2
import utils.crypto.tea.TeaProvider
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray


@OptIn(ExperimentalSerializationApi::class)
fun buildSSOClientReq(signResponse: SignResponse): ByteArray{
    val protoBuf = ProtoBuf{
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

fun decodeHeader(bin: ByteArray,bot: BotCommon):ByteArray{
    var allBody: ByteArray
    val up = Buffer()
    val type: Short
    up.write(bin).apply {
        up.readInt()
        up.readInt()  // 00 00 00 0A
        type = up.readShort() // 02 00
        // up.readInt()  // 00 00 00 00
        val uinLen = up.readInt() - 4
        up.readByteArray(uinLen)
        allBody = up.readByteArray()
    }
    //println("TGTGTKey:${bot.keystore.WLoginSigs.TGTGTKey.toHexString()},ShareKey:${bot.keystore.ECDH.shareKey.toHexString()},RandomKey:${bot.keystore.WLoginSigs.RandomKey.toHexString()}")
    if (!bot.success) {
        allBody = TeaProvider.decrypt(allBody, ByteArray(16) { 0 })
    } else
        {
        allBody = TeaProvider.decrypt(allBody, bot.keystore.WLoginSigs.D2Key)
    }
    return allBody
}

fun readFuncBuffer(bin: ByteArray){
    println("Receive:"+bin.toHexString())
}