package proto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SsoClientReq(
    @ProtoNumber(9)  val filed1: Int = 0,
    @ProtoNumber(11) val filed8: Int = 2052,
    @ProtoNumber(12) val qimei: String = "b9a1be24277f73daef6d88ca100016d1730c",
    @ProtoNumber(14) val filed9: Int = 0,
    @ProtoNumber(18) val filed2: Int = 0,
    @ProtoNumber(19) val filed3: Int = 1,
    @ProtoNumber(20) val filed4: Int = 1,
    @ProtoNumber(21) val filed5: Int = 0,
    @ProtoNumber(23) val plain1: SsoClientReqPlain1= SsoClientReqPlain1("client_conn_seq",(System.currentTimeMillis() / 1000).toString()),
    @ProtoNumber(24) val plain2: SsoClientReqPlain2,
    @ProtoNumber(26) val filed6: Int = 100,
    @ProtoNumber(28) val filed7: Int = 3
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SsoClientReqPlain1(
    @ProtoNumber(1) val filed1: String,
    @ProtoNumber(2) val filed2: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SsoClientReqPlain2(
    @ProtoNumber(1) val sign: ByteArray,
    @ProtoNumber(2) val token: ByteArray,
    @ProtoNumber(3) val extra: ByteArray
)

