package internal.service.system

import botsetting.BotCommon
import internal.packet.login.TlvBuilder
import internal.packet.state.online
import internal.packet.system.bufferHead
import internal.service.store.SessionManager
import utils.crypto.tea.TeaProvider
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray


fun getLogin(botCommon: BotCommon): Int {
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
    var sendBody = frameBuffer.readByteArray()
    sendBody = bufferHead(botCommon, sendBody, "wtlogin.login")
    println("firstBin:${sendBody.toHexString()}")
    val retBody = botCommon.client.send(sendBody)
    return if (retBody != null) {
        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

fun unPacketWTLogin(bin: ByteArray, bot: BotCommon): Int {
    var allBody: ByteArray
    val up = Buffer()
    var code: Int
    up.write(bin).apply {
        val len = up.readInt() - 4 //len
        up.readByteArray(len)
        up.readInt()
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

fun wtLogin(botCommon: BotCommon) {
    botCommon.client.connect()
    var code = getLogin(botCommon)

    while (true) {
        println("CheckCode:$code")
        when (code) {

            -1 -> {
                println("Login failed, the server did not return any data")
                break
            }

            0 -> {
                println("Login Success!")
                SessionManager.saveSigs(botCommon)
                botCommon.success = true
                if(online(botCommon)){
                    println("Online success")
                }else{
                    println("Online failed")
                }
                break
            }

            2 -> {
                println("Need Check Ticket:")
                println(botCommon.keystore.Iframe.url)
                println("Please enter the ticket")
                botCommon.keystore.Iframe.ticket = readln()
                code = getLoginSubmitTicekt(botCommon)
                continue
            }

            160 -> {
                println("Need Check Phone")
                code = getLoginSendSMS(botCommon)
                if (code == 160) {
                    println("Please enter the SMSCode")
                    botCommon.keystore.Iframe.ticket = readln()
                    code = getLoginCheckSMS(botCommon)
                    continue
                }
                break
            }

            239 -> {
                println("Need Check Phone")
                code = getLoginSendSMS(botCommon)
                if (code == 160) {
                    println("Please enter the SMSCode")
                    botCommon.keystore.Iframe.ticket = readln()
                    code = getLoginCheckSMS(botCommon)
                    continue
                }
                break
            }

            else -> {
                println("ErrorCode:${code},${botCommon.keystore.ErrorTitle},${botCommon.keystore.ErrorMessage}")
                break
            }
        }
    }
    println("Login End")

}

fun getTlvData(bin: ByteArray, botCommon: BotCommon, count: Int) {
    println("Tlvdata:${bin.toHexString()}")
    val up = Buffer().apply { write(bin) }
    for ( i in 0 until count) {
        val tag = up.readShort().toInt() and 0xFFFF
        val length = up.readShort().toInt() and 0xFFFF
        val value = up.readByteArray(length)
        println("tag:${tag.toHexString()},value:${value.toHexString()}")
        when (tag) {
            0x146 -> {
                val t = Buffer().apply { write(value) }
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
                println("A2:${value.toHexString()}")
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
                println("D2Key:${value.toHexString()}")
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
                val tmp = TeaProvider.decrypt(value,botCommon.keystore.WLoginSigs.TGTGTKey)
                val up = Buffer().apply { write(tmp) }
                val c = up.readShort().toInt()
                getTlvData(up.readByteArray(),botCommon,c)
            }
        }
    }

}

fun getLoginSubmitTicekt(botCommon: BotCommon): Int {
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
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login")
    println("secondBin:${sendBody.toHexString()}")
    val retBody = botCommon.client.send(sendBody)
    return if (retBody != null) {

        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

fun getLoginSendSMS(botCommon: BotCommon): Int {
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
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login")
    println("thirdBin:${sendBody.toHexString()}")
    val retBody = botCommon.client.send(sendBody)
    return if (retBody != null) {
        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}

fun getLoginCheckSMS(botCommon: BotCommon): Int {
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
    sendBody=bufferHead(botCommon, sendBody, "wtlogin.login")
    println("fourthBin:${sendBody.toHexString()}")
    val retBody = botCommon.client.send(sendBody)

    return if (retBody != null) {

        unPacketWTLogin(retBody, botCommon)
    } else {
        -1
    }
}