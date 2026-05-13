package internal.service.system

import botsetting.BotCommon
import botsetting.BotManager
import internal.packet.login.TlvBuilder
import internal.packet.system.bufferHead
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import utils.crypto.tea.TeaProvider
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

private fun BotCommon.log(): KLogger = KotlinLogging.logger("[${this.keystore.uin}]")
suspend fun getLogin(botCommon: BotCommon): Int {
    val buffer = Buffer().apply {
        writeShort(8001)
        writeShort(2064)
        writeShort(1)
        writeInt(botCommon.keystore.uin.toInt())
        writeFully("0387000000000200000000000000000201".hexToByteArray())
        writeFully(botCommon.keystore.WLoginSigs.RandomKey)
        writeShort(305)
        writeShort(2)
        writeShort(botCommon.keystore.ECDH.publicKey.size.toShort())
        writeFully(botCommon.keystore.ECDH.publicKey)
        val tlv = TlvBuilder(botCommon)
        tlv.tlv18()
        tlv.tlv1()
        tlv.tlv106()
        tlv.tlv116()
        tlv.tlv100()
        tlv.tlv107()
        tlv.tlv142()
        tlv.tlv144()
        tlv.tlv145()
        tlv.tlv147()
        tlv.tlv154(botCommon.keystore.SsoSeq)
        tlv.tlv141()
        tlv.tlv8()
        tlv.tlv511()
        tlv.tlv187()
        tlv.tlv188()
        tlv.tlv191()
        tlv.tlv177()
        tlv.tlv516()
        tlv.tlv521()
        tlv.tlv525()
        tlv.tlv544("9")
        tlv.tlv545()
        tlv.tlv548()
        tlv.tlv553()
        tlv.tlv542()
        val tlvData = tlv.buildWTLogin()
        val encryptedTlvs = TeaProvider.encrypt(tlvData, botCommon.keystore.ECDH.shareKey)
        writeFully(encryptedTlvs)
    }
    val innerData = buffer.readByteArray()
    val frameBuffer = Buffer().apply {
        writeByte(0x02.toByte())
        writeShort((innerData.size + 4).toShort())
        writeFully(innerData)
        writeByte(0x03.toByte())
    }
    val seq = botCommon.keystore.SsoSeq
    var sendBody = frameBuffer.readByteArray()
    sendBody = bufferHead(botCommon, sendBody, "wtlogin.login",seq)
    val retBody = botCommon.client.send(seq,sendBody)
    return if (retBody != null) {
        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

fun unPacketWTLogin(bin: ByteArray, bot: BotCommon): Int {
    var allBody: ByteArray
    val up = Buffer()
    var code: Int = -1
    up.write(bin).apply {
        up.readByte()  // 02
        up.readShort()
        up.readShort() // 1F 41
        up.readShort() // 08 10
        up.readShort() // 00 01
        up.readInt()   // uin
        up.readShort() // 00 00
        code = up.readByte().toInt() and 0xFF
        allBody = up.readByteArray((up.size - 1).toInt())
        allBody = TeaProvider.decrypt(allBody, bot.keystore.ECDH.shareKey)
    }
    if (code == 0){
        up.write(allBody).apply {
            up.readShort()  //00 02
            up.readShort()  //00 00
            up.readShort()   //02
            allBody = up.readByteArray()
            getTlvData(allBody,bot,2)
        return 0
        }
    }

    up.write(allBody).apply {
        up.readInt()
        val count = up.readShort().toInt()
        getTlvData(up.readByteArray(), bot, count)
    }
    return code
}

suspend fun BotCommon.wtLogin() {

    val conn = this.client.connect()
    if (!conn){
        this.log.error { "Login failed" }
        this.loginInfo.code = -1
        this.loginInfo.title = "Login Failed"
        this.loginInfo.msg = "Connect Server Failed"
        log.error { "Login failed, Connect Server Failed" }
        return
    }
    val code = getLogin(this)

    while (true) {
        this.log.debug { "CheckCode: $code" }
        when (code) {

            -1 -> {
                this.loginInfo.code = -2
                this.loginInfo.title = "Login Failed"
                this.loginInfo.msg = "Sign is not online"
                this.client.disconnect()
                log.error { "Login failed, Sign is not online" }
                break
            }

            0 -> {
                this.log.info { "Login Success: ${0}" }
                BotManager.saveAccount(this.keystore.uin)
                this.loginInfo.code = 200
                this.loginInfo.msg = "Login Success"
                this.success = true
//                if(online()){
//                    this.log.info { "Register Success" }
//                }else{
//                    this.log.warn { "Register Failed" }
//                    this.client.disconnect()
//                }
                break
            }

            2 -> {
                this.log.warn { "Need Ticket" }
                this.loginInfo.code = 2
                this.loginInfo.msg = "Need Ticket"
                this.loginInfo.title = "Need Ticket"
                this.loginInfo.data = this.keystore.Iframe.url
//                this
//                this.log.info { "URL: ${this.keystore.Iframe.url}" }
//                this.log.info {"Please send Ticket:"}
//                this.keystore.Iframe.ticket = readln()
//                code = getLoginSubmitTicekt(this)
                break
            }

            160,239 -> {
                this.log.warn { "Need SMS Code" }
                this.loginInfo.code = 160
                this.loginInfo.msg = "Need SMS Code"
                this.loginInfo.title = "Need SMS Code"
//                code = getLoginSendSMS(this)
//                if (code == 160) {
//                    this.log.warn { "Please send SMS Code:" }
//                    this.keystore.Iframe.ticket = readln()
//                    code = getLoginCheckSMS(this)
//                    continue
//                }
                break
            }
            else -> {
                this.log.error { "Login Failed: Code=$code, Title=${this.keystore.ErrorTitle}, Message=${this.keystore.ErrorMessage}" }
                break
            }
        }
    }
}

fun getTlvData(bin: ByteArray, botCommon: BotCommon, count: Int) {
    val log = botCommon.log()
    log.trace { "Unpacking TLV : ${bin.toHexString()}" }
    val up = Buffer().apply { write(bin) }
    for ( i in 0 until count) {
        val tag = up.readShort().toInt() and 0xFFFF
        val length = up.readShort().toInt() and 0xFFFF
        val value = up.readByteArray(length)
        log.trace { "Tag: 0x${tag.toHexString().uppercase()}, Length: $length" }
        when (tag) {
            0x146 -> {
                val t = Buffer().apply { write(value) }
                t.readInt()
                var len = t.readShort()
                botCommon.keystore.ErrorTitle = t.readByteArray(len.toInt()).decodeToString()
                len = t.readShort()
                botCommon.keystore.ErrorMessage = t.readByteArray(len.toInt()).decodeToString()
                break
            }

            0x103 -> {
                botCommon.keystore.WLoginSigs.StWeb = value
            }

            0x143 -> {
                botCommon.keystore.WLoginSigs.D2 = value
            }

            0x108 -> {
                botCommon.keystore.WLoginSigs.Ksid = value
            }

            0x10A -> {
                botCommon.keystore.WLoginSigs.A2 = value
                log.debug {"A2:${value.toHexString()}"}
            }

            0x10C -> {
                botCommon.keystore.WLoginSigs.A1Key = value
            }

            0x10D -> {
                botCommon.keystore.WLoginSigs.A2Key = value
            }

            0x10E -> {
                botCommon.keystore.WLoginSigs.StKey = value
            }

            0x114 -> {
                botCommon.keystore.WLoginSigs.St = value
            }

            0x120 -> {
                botCommon.keystore.WLoginSigs.SKey = value
            }

            0x133 -> {
                botCommon.keystore.WLoginSigs.WtSessionTicket = value
            }

            0x134 -> {
                botCommon.keystore.WLoginSigs.WtSessionTicketKey = value
            }

            0x305 -> {
                botCommon.keystore.WLoginSigs.D2Key = value
                log.debug {"D2Key:${value.toHexString()}"}
            }

            0x106 -> {
                botCommon.keystore.WLoginSigs.A1 = value
            }

            0x16A -> {
                botCommon.keystore.WLoginSigs.NoPicSig = value
            }

            0x16D -> {
                botCommon.keystore.WLoginSigs.SuperKey = value
            }

            0x192 -> {
                botCommon.keystore.Iframe.url = value.decodeToString()
            }

            0x104 -> {
                botCommon.keystore.State.Tlv104 = value
            }

            0x547 -> {
                botCommon.keystore.State.Tlv547 = value
            }

            0x174 -> {
                botCommon.keystore.State.Tlv174 = value
            }

            0x119 -> {
                val tmp: ByteArray = TeaProvider.decrypt(value,botCommon.keystore.WLoginSigs.TGTGTKey)
                val up = Buffer().apply { write(tmp) }
                val c = up.readShort().toInt()
                getTlvData(up.readByteArray(),botCommon,c)
            }
        }
    }

}

suspend fun getLoginSubmitTicekt(botCommon: BotCommon): Int {
    val buffer = Buffer().apply {
        writeShort(8001)
        writeShort(2064)
        writeShort(1)
        writeInt(botCommon.keystore.uin.toInt())
        writeFully("0307000000000200000000000000000201".hexToByteArray())
        writeFully(botCommon.keystore.WLoginSigs.RandomKey)
        writeShort(305)
        writeShort(2)
        writeShort(botCommon.keystore.ECDH.publicKey.size.toShort())
        writeFully(botCommon.keystore.ECDH.publicKey)
        val tlv = TlvBuilder(botCommon)
        tlv.tlv193()
        tlv.tlv8()
        tlv.tlv104()
        tlv.tlv116()
        tlv.tlv547()
        tlv.tlv544("2")
        tlv.tlv542()
        val tlvData = tlv.buildWTLoginSubmitTicket()
        val encryptedTlvs = TeaProvider.encrypt(tlvData, botCommon.keystore.ECDH.shareKey)
        writeFully(encryptedTlvs)
    }
    val innerData = buffer.readByteArray()
    val frameBuffer = Buffer().apply {
        writeByte(0x02.toByte())
        writeShort((innerData.size + 4).toShort())
        writeFully(innerData)
        writeByte(0x03.toByte())
    }
    var sendBody = frameBuffer.readByteArray()
    val seq = botCommon.keystore.SsoSeq
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login",seq)
    val retBody = botCommon.client.send(seq,sendBody)
    return if (retBody != null) {

        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

suspend fun getLoginSendSMS(botCommon: BotCommon): Int {
    val buffer = Buffer().apply {
        writeShort(8001)
        writeShort(2064)
        writeShort(1)
        writeInt(botCommon.keystore.uin.toInt())
        writeFully("0307000000000200000000000000000201".hexToByteArray())
        writeFully(botCommon.keystore.WLoginSigs.RandomKey)
        writeShort(305)
        writeShort(2)
        writeShort(botCommon.keystore.ECDH.publicKey.size.toShort())
        writeFully(botCommon.keystore.ECDH.publicKey)
        val tlv = TlvBuilder(botCommon)
        tlv.tlv8()
        tlv.tlv104()
        tlv.tlv116()
        tlv.tlv174()
        tlv.tlv17A()
        tlv.tlv197()
        val tlvData = tlv.buildWTLoginSendSMS()
        val encryptedTlvs = TeaProvider.encrypt(tlvData, botCommon.keystore.ECDH.shareKey)
        writeFully(encryptedTlvs)
    }
    val innerData = buffer.readByteArray()
    val frameBuffer = Buffer().apply {
        writeByte(0x02.toByte())
        writeShort((innerData.size + 4).toShort())
        writeFully(innerData)
        writeByte(0x03.toByte())
    }
    var sendBody = frameBuffer.readByteArray()
    val seq = botCommon.keystore.SsoSeq
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login",seq)
    val retBody = botCommon.client.send(seq,sendBody)
    return if (retBody != null) {
        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

suspend fun getLoginCheckSMS(botCommon: BotCommon): Int {
    val buffer = Buffer().apply {
        writeShort(8001)
        writeShort(2064)
        writeShort(1)
        writeInt(botCommon.keystore.uin.toInt())
        writeFully("0307000000000200000000000000000201".hexToByteArray())
        writeFully(botCommon.keystore.WLoginSigs.RandomKey)
        writeShort(305)
        writeShort(2)
        writeShort(botCommon.keystore.ECDH.publicKey.size.toShort())
        writeFully(botCommon.keystore.ECDH.publicKey)
        val tlv = TlvBuilder(botCommon)
        tlv.tlv8()
        tlv.tlv104()
        tlv.tlv116()
        tlv.tlv174()
        tlv.tlv17C()
        tlv.tlv401()
        tlv.tlv198()
        tlv.tlv542()
        tlv.tlv544("9")
        tlv.tlv553()
        val tlvData = tlv.buildWTLoginCheckSMS()
        val encryptedTlvs = TeaProvider.encrypt(tlvData, botCommon.keystore.ECDH.shareKey)
        writeFully(encryptedTlvs)
    }
    val innerData = buffer.readByteArray()
    val frameBuffer = Buffer().apply {
        writeByte(0x02.toByte())
        writeShort((innerData.size + 4).toShort())
        writeFully(innerData)
        writeByte(0x03.toByte())
    }
    var sendBody = frameBuffer.readByteArray()
    val seq = botCommon.keystore.SsoSeq
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login",seq)
    val retBody = botCommon.client.send(seq,sendBody)
    return if (retBody != null) {
        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}