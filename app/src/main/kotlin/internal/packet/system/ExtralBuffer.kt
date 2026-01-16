package com.kurome.app.internal.packet.system

import botsetting.BotCommon
import com.kurome.app.utils.crypto.tea.TeaProvider

import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeString

fun bufferHead(bot: BotCommon, bin: ByteArray, cmd: String): ByteArray {
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
        writeInt(4)
        //writeFully(getRandomBytes(4))
        writeInt(bot.keystore.androidId.length + 4)
        writeString(bot.keystore.androidId)
        writeInt(4)
        writeShort((("||A"+bot.appinfo.currentVersion).length + 2 ).toShort())
        writeString( "||A"+bot.appinfo.currentVersion)
        val signData = getSecSign(bot, cmd, bin.toHexString())?.let { buildSSOClientReq(it) }

        if (signData != null) {
            writeInt(signData.size + 4)
            writeFully(signData)
        }
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

    val bin1 = Buffer().apply {
        writeInt(10)
        writeByte(2.toByte())
        writeInt(4)
        writeInt(0)
        writeByte((bot.keystore.uin.toString().length + 4).toByte())
        writeString(bot.keystore.uin.toString())
        writeFully(encryptedData)

    }
    val bin = bin1.readByteArray()

    val head = Buffer().apply {
        writeInt(bin.size + 4)
        writeFully(bin)
    }

    return head.readByteArray()
}

