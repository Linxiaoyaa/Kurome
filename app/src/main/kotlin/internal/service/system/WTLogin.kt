package com.kurome.app.internal.service.system

import botsetting.BotCommon
import com.kurome.app.internal.packet.login.TlvBuilder
import com.kurome.app.internal.packet.system.bufferHead
import com.kurome.app.utils.crypto.tea.TeaProvider
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlin.text.toByteArray

suspend fun getLogin(botCommon: BotCommon) : ByteArray{
    val buffer = Buffer().apply {
        writeShort(8001)
        writeShort(2064)
        writeShort(1)
        writeInt(botCommon.keystore.uin.toInt())
        writeFully("0387000000000200000000000000000201".hexToByteArray())
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
        tlv.tlv154()
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
        tlv.tlv544()
        tlv.tlv545()
        tlv.tlv548()
        tlv.tlv553()
        tlv.tlv542()
        val tlvData = tlv.build()
        val encryptedTlvs = TeaProvider.encrypt(tlvData, botCommon.keystore.ECDH.shareKey)
        writeFully(encryptedTlvs)
    }
    val innerData = buffer.readByteArray()
    val frameBuffer = Buffer().apply {
        writeByte(0x02.toByte()) // 开始符
        writeShort((innerData.size + 4).toShort())
        writeFully(innerData)
        writeByte(0x03.toByte()) // 结束符
    }
    println(frameBuffer.readByteArray().toHexString())
    return bufferHead(botCommon, frameBuffer.readByteArray(), "wtlogin.login")

}