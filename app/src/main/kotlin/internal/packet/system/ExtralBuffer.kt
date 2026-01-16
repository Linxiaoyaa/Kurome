package com.kurome.app.internal.packet.system

import botsetting.BotCommon
import com.kurome.app.utils.crypto.tea.TeaProvider
import com.kurome.app.utils.getRandomBytes
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeString

suspend fun bufferHead(bot: BotCommon, bin: ByteArray, cmd: String): ByteArray {
    val pack = Buffer().apply {
        writeInt(114514)
        writeInt(bot.appinfo.subAppId.toInt())
        writeInt(bot.appinfo.subAppId.toInt())
        writeInt(16777216)
        writeInt(0)
        writeInt(256)
        writeInt(4)
        writeInt(cmd.toByteArray().size + 4)
        writeString(cmd)
        writeInt(8)
        writeFully(getRandomBytes(4))
        writeInt(bot.keystore.androidId.length + 4)
        writeString(bot.keystore.androidId)
        writeInt(4)
        writeShort((bot.appinfo.currentVersion.length + 4).toShort())
        writeString( bot.appinfo.currentVersion)
        val signdata = getSecSign(bot, cmd, bin.toHexString())?.let { buildSSOClientReq(it) }
        if (signdata != null) {
            writeInt(signdata.size + 4)
        }
        signdata?.let { writeFully(it) }
    }
    var data = pack.readByteArray()
    val bin0 = Buffer().apply {
        writeInt(data.size + 4)
        writeFully(data)
    }
    data = bin0.readByteArray()
    bin0.clear()

    bin0.writeFully(data)
    bin0.writeInt(bin.size + 4)
    bin0.writeFully(bin)
    data = bin0.readByteArray()

    val encryptedData = TeaProvider.encrypt(data, ByteArray(16) { 0 })
    val bin1 = Buffer()
    bin1.writeInt(10)
    bin1.writeByte(2.toByte())
    bin1.writeInt(4)
    bin1.writeInt(0)
    bin1.writeByte((bot.keystore.uin.toString().length + 4).toByte()) // 长度前缀占 1 字节
    bin1.writeString( bot.keystore.uin.toString())
    bin1.writeFully(encryptedData)
    val head = Buffer().apply {
        writeInt(bin1.readByteArray().size + 4)
        writeFully(bin1.readByteArray())
    }
    return head.readByteArray()
}

