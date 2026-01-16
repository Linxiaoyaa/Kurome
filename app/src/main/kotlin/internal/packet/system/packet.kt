package com.kurome.app.internal.packet.system
import kotlinx.serialization.protobuf.ProtoBuf
import com.kurome.app.proto.SsoClientReq
import com.kurome.app.proto.SsoClientReqPlain2

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray


@OptIn(ExperimentalSerializationApi::class)
fun buildSSOClientReq(signResponse: SignResponse): ByteArray{
    val PB = ProtoBuf{
        encodeDefaults = true
    }
    val request = SsoClientReq(
        plain2 = SsoClientReqPlain2(
            sign = signResponse.data.sign.hexToByteArray(),
            token = signResponse.data.token.hexToByteArray(),
            extra = signResponse.data.extra.hexToByteArray()
        ),

    )
    return PB.encodeToByteArray(request)
}