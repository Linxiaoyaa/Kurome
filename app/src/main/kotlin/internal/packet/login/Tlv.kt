package com.kurome.app.internal.packet.login

import botsetting.BotCommon
import com.kurome.app.utils.crypto.tea.TeaProvider
import com.kurome.app.utils.getRandomBytes
import com.kurome.app.utils.md5
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeString
import kotlin.text.toByteArray

class TlvBuilder(val bot: BotCommon) {

    private val mainBuffer = Buffer()

    private var tlvCount: Int = 0


    private fun defineTlv(tag: Int, block: Buffer.() -> Unit) {
        tlvCount++
        mainBuffer.writeShort(tag.toShort()) // 写入 Tag (2字节)


        val contentBuffer = Buffer()
        contentBuffer.block()
        val content = contentBuffer.readByteArray()

        mainBuffer.writeShort(content.size.toShort())
        mainBuffer.writeFully(content)
    }

    fun tlv18() = defineTlv(0x18) {
        writeShort(1)
        writeInt(0x00000600)
        writeInt(bot.appinfo.appId.toInt())
        writeInt(0)
        writeInt(bot.keystore.uin.toInt())
        writeShort(0)
        writeShort(0)
    }

    fun tlv1() = defineTlv(0x1) {
        writeShort(1)
        writeFully(getRandomBytes(4))
        writeInt(bot.keystore.uin.toInt())
        writeInt((System.currentTimeMillis() / 1000).toInt())
        writeInt(0)
        writeShort(0)
    }

    fun tlv106() = defineTlv(0x106) {
        val plainBuffer = Buffer().apply {
            writeShort(4)
            writeFully(getRandomBytes(4))
            writeInt(20)
            writeInt(16)
            writeInt(0)
            writeInt(0)
            writeInt(bot.keystore.uin.toInt())
            writeInt((System.currentTimeMillis() / 1000).toInt())
            writeInt(0)
            writeByte(1)
            writeFully(bot.keystore.passwordKey)
            writeFully(bot.keystore.WLoginSigs.TGTGTKey)
            writeInt(0)
            writeByte(1)
            writeFully(bot.keystore.guid.hexToByteArray())
            writeInt(bot.appinfo.subAppId.toInt())
            writeInt(1)
            writeShort(bot.keystore.uin.toString().length.toShort())
            writeString(bot.keystore.uin.toString())
            writeShort(0)
        }
        val encryptedData = TeaProvider.encrypt(plainBuffer.readByteArray(), bot.keystore.password2Key)
        writeFully(encryptedData)
    }

    fun tlv116() = defineTlv(0x116) {
        writeByte(0)
        writeInt(bot.appinfo.sdkInfo.miscBitMap.toInt())
        writeInt(bot.appinfo.sdkInfo.subSigMap.toInt())
        writeByte(1)
        writeInt(1600000226)
    }
    fun tlv547() = defineTlv(0x547){
        writeFully("01020101000100000080C8714892C2F6B2EB64033AB1D5E565A0C382B151132D44C6EF1BC8C0F119CEEC8FC918A4BBD1B6D3590D70BB47297309C726204A5A6973F74E20B9273C4C7B4E6167EBF6EDCA3C2134F419980C7BC22EA6242AC5BA754DD109B6F9AE2A9D7ACEF83EE6C673ED21E893D7005C5868724D7BE1F3CD5F1521F4D2CA43D9367B876A00209C94371A86B9B968E61534FCAC3E928412F0FF1145CDEA5C335FD05B2C8A285300AC01020102000100000080C8714892C2F6B2EB64033AB1D5E565A0C382B151132D44C6EF1BC8C0F119CEEC8FC918A4BBD1B6D3590D70BB47297309C726204A5A6973F74E20B9273C4C7B4E6167EBF6EDCA3C2134F419980C7BC22EA6242AC5BA754DD109B6F9AE2A9D7ACEF83EE6C673ED21E893D7005C5868724D7BE1F3CD5F1521F4D2CA43D9367B876A00209C94371A86B9B968E61534FCAC3E928412F0FF1145CDEA5C335FD05B2C8A28530080C8714892C2F6B2EB64033AB1D5E565A0C382B151132D44C6EF1BC8C0F119CEEC8FC918A4BBD1B6D3590D70BB47297309C726204A5A6973F74E20B9273C4C7B4E6167EBF6EDCA3C2134F419980C7BC22EA6242AC5BA754DD109B6F9AE2A9D7ACEF83EE6C673ED21E893D7005C5868724D7BE1F3CD5F1521F4D2CA43D9367B94F7000003A900000D8D".hexToByteArray())
    }
    fun tlv174() = defineTlv(0x174){
        writeFully(bot.keystore.State.Tlv174)
    }
    fun tlv17A() = defineTlv(0x17A){
        writeInt(9)
    }
    fun tlv17C() = defineTlv(0x17C){
        //短信验证码
        writeShort(bot.keystore.Iframe.ticket.length.toShort())
        writeString(bot.keystore.Iframe.ticket)
    }
    fun tlv197() = defineTlv(0x197){
        writeByte(0)
    }
    fun tlv100() = defineTlv(0x100) {
        writeShort(1)
        writeInt(bot.appinfo.ssoVersion)
        writeInt(bot.appinfo.appId.toInt())
        writeInt(bot.appinfo.subAppId.toInt())
        writeInt(0)
        writeInt(bot.appinfo.sdkInfo.mainSigMap.toInt())
    }
    fun tlv401() =defineTlv(0x401){
        writeFully(getRandomBytes(16))
    }
    fun tlv198() =defineTlv(0x198){
        writeByte(0)
    }

    fun tlv107() = defineTlv(0x107) {
        writeInt(0)
        writeShort(1)
    }

    fun tlv142() = defineTlv(0x142) {
        writeInt(bot.appinfo.packageName.length)
        writeString(bot.appinfo.packageName)
    }

    fun tlv109() = defineTlv(0x109) {
        writeFully(md5(bot.keystore.androidId.toByteArray()))
    }

    fun tlv52D() = defineTlv(0x52D) {
        writeFully(
            "0A10623163312D302E312D34393438383134129B014C696E75782076657273696F6E20342E31392E3131302028616E64726F69642D6275696C644077706975352E686F742E636F72702E676F6F676C652E636F6D2920286763632076657273696F6E20342E36203230313230313036202870726572656C6561736529202847434329292023393520534D5020505245454D505420536174204170722032392030323A32313A34372043535420323032331A0352454C22093930303233303532392A4F73616D73756E672F534D2D473935354E2F647265616D326C74656B733A392F4E524439304D2E473935354E4B535531415144432F3930303233303532393A757365722F72656C656173652D6B657973322435323761656339642D646438312D343134372D396436372D6437613663636265323633383A103863623736323634636130326364636342004A3B534D2D473935354E2D757365722039204E524439304D2E473935354E4B53553141514443203930303233303532392072656C656173652D6B657973".hexToByteArray()
        )
    }

    fun tlv124() = defineTlv(0x124) {
        writeShort(7)
        writeString("android") //system_type
        writeShort(2)
        writeString("16") //system_os
        writeShort(2)
        writeShort(16)
        writeString("China Mobile GSM")
        writeInt(4)
        writeString("wifi")
    }

    fun tlv128() = defineTlv(0x128) {
        writeShort(0)
        writeByte(0)
        writeByte(1)
        writeByte(0)
        writeInt(16777216)
        writeShort(bot.keystore.deviceName.length.toShort())
        writeString(bot.keystore.deviceName)//这里应该是型号
        writeShort(16)
        writeFully(bot.keystore.guid.hexToByteArray())
        writeShort(bot.keystore.deviceName.length.toShort())
        writeString(bot.keystore.deviceName) //这里应该是品牌
    }

    fun tlv16E() = defineTlv(0x16E) {
        writeString(bot.keystore.deviceName)
    }

    fun tlv144() = defineTlv(0x144) {

        val subBuilder = TlvBuilder(bot)
        subBuilder.tlv109()
        subBuilder.tlv52D()
        subBuilder.tlv124()
        subBuilder.tlv128()
        subBuilder.tlv16E()
        val subData = subBuilder.build()
        val encryptedData = TeaProvider.encrypt(subData, bot.keystore.WLoginSigs.TGTGTKey)
        writeFully(encryptedData)
    }

    fun tlv145() = defineTlv(0x145){
        writeFully(bot.keystore.guid.hexToByteArray())
    }

    fun tlv147() = defineTlv(0x147){
        writeInt(16)
        writeShort(bot.appinfo.ptVersion.length.toShort())
        writeString(bot.appinfo.ptVersion)
        writeShort(16)
        writeFully("A6B745BF24A2C277527716F6F36EB68D".hexToByteArray())
    }

    fun tlv154() = defineTlv(0x154){
        writeInt(114514) //seq
    }

    fun tlv141() = defineTlv(0x141){
        writeShort(1)
        writeShort(16)
        writeString("China Mobile GSM")
        writeShort(2)
        writeShort(4)
        writeString("wifi")
    }
    fun tlv8() = defineTlv(0x8){
        writeInt(0)
        writeShort(2052)
        writeShort(0)
    }
    fun tlv511() = defineTlv(0x511){
        writeShort(0x0010)
        val domains = listOf(
            "tenpay.com",
            "openmobile.qq.com",
            "docs.qq.com",
            "connect.qq.com",
            "qzone.qq.com",
            "vip.qq.com",
            "gamecenter.qq.com",
            "qun.qq.com",
            "game.qq.com",
            "qqweb.qq.com",
            "ti.qq.com",
            "office.qq.com",
            "mail.qq.com",
            "mma.qq.com",
            "qidian.qq.com",
            "clt.qq.com"
        )
        domains.forEach { domain ->
            writeByte(0x01)
            val domainBytes = domain.toByteArray(Charsets.UTF_8)
            writeShort(domainBytes.size.toShort())
            writeFully(domainBytes)
        }
    }

    fun tlv187() =defineTlv(0x187){
        writeFully(md5(bot.keystore.mac.toByteArray()))
    }
    fun tlv188() = defineTlv(0x188){
        writeFully(md5(bot.keystore.androidId.toByteArray()))
    }

    fun tlv191() = defineTlv(0x191){
        writeFully("82".hexToByteArray())
    }

    fun tlv177() = defineTlv(0x177){
        writeByte(1)
        writeInt((System.currentTimeMillis()/1000).toInt())
        writeShort(bot.appinfo.sdkInfo.sdkVersion.length.toShort())
        writeString(bot.appinfo.sdkInfo.sdkVersion)
    }

    fun tlv516() =defineTlv(0x516){
        writeInt(0)
    }

    fun tlv521() =defineTlv(0x521){
        writeInt(0)
        writeShort(0)
    }
    fun tlv525() = defineTlv(0x525){
        writeFully("0001053600020100".hexToByteArray())
    }

    fun tlv544() = defineTlv(0x544){
        writeFully("EF78390C78030DFC9AA300000B15E152DA16D110E4CAB1BE79B5D6F93D3830327023C4DB24B79D03065ED388EE342129F816051761006AC222C57EBD392C826DB1F80D766CE213579EA2C8BC4407C413D617F1EFD1FEE3ED1B451525".hexToByteArray())
    }
    fun tlv545() = defineTlv(0x545){
        writeString(bot.keystore.qimei)
    }

    fun tlv548() = defineTlv(0x548){
        writeFully("01020101000A000000806D21468F9AAF5195D91C60A4E82C70B4F83C80C4084C90D4185CA0E4286CB0F4387CC004488CD014589CE02468ACF03478BC004488CC105498DC2064A8EC3074B8FC4084C80C5094D81C60A4E82C70B4F83C80C4084C90D4185CA0E4286CAFF3377BBF03478BCF13579BDF2367ABEF3377BBFF4387CB0F5397DB1F63A7EB2F730020F6495E38A90C8CC16BDAC44D96EF1D159AF61F96AB999702D19A0952630612E200AC01020102000A000000806D21468F9AAF5195D91C60A4E82C70B4F83C80C4084C90D4185CA0E4286CB0F4387CC004488CD014589CE02468ACF03478BC004488CC105498DC2064A8EC3074B8FC4084C80C5094D81C60A4E82C70B4F83C80C4084C90D4185CA0E4286CAFF3377BBF03478BCF13579BDF2367ABEF3377BBFF4387CB0F5397DB1F63A7EB2F730020F6495E38A90C8CC16BDAC44D96EF1D159AF61F96AB999702D19A0952630612E200806D21468F9AAF5195D91C60A4E82C70B4F83C80C4084C90D4185CA0E4286CB0F4387CC004488CD014589CE02468ACF03478BC004488CC105498DC2064A8EC3074B8FC4084C80C5094D81C60A4E82C70B4F83C80C4084C90D4185CA0E4286CAFF3377BBF03478BCF13579BDF2367ABEF3377BBFF4387CB0F5397DB1F63A7EB56830000005A00002710".hexToByteArray())
    }

    fun tlv553() =defineTlv(0x553){
        writeFully("0A93010A9001413446443332413233373246343030304434334539314342373041313343443439364445324443453946353230454337443543314236393035353737333933453743373442443438353037413734433043393333424131313937334644424246313938323838303438444639304239383842324132363241373533413333434330324431433945444434443236454634".hexToByteArray())
    }

    fun tlv542() =defineTlv(0x542){
        writeFully("4A0460017801880101".hexToByteArray())

    }
    fun tlv193() = defineTlv(0x193){
        writeFully(bot.keystore.Iframe.ticket.toByteArray())
    }
    fun tlv104() = defineTlv(0x104){
        writeFully(bot.keystore.State.Tlv104)
    }
    fun buildWTLogin(): ByteArray {
        val finalResult = Buffer()
        finalResult.writeShort(9)
        finalResult.writeShort(tlvCount.toShort()) // 先写总数
        finalResult.writeFully(mainBuffer.readByteArray()) // 再写所有内容
        return finalResult.readByteArray()
    }
    fun buildWTLoginSubmitTicket(): ByteArray {
        val finalResult = Buffer()
        finalResult.writeShort(2)
        finalResult.writeShort(tlvCount.toShort()) // 先写总数
        finalResult.writeFully(mainBuffer.readByteArray()) // 再写所有内容
        return finalResult.readByteArray()
    }
    fun buildWTLoginSendSMS(): ByteArray {
        val finalResult = Buffer()
        finalResult.writeShort(8)
        finalResult.writeShort(tlvCount.toShort()) // 先写总数
        finalResult.writeFully(mainBuffer.readByteArray()) // 再写所有内容
        return finalResult.readByteArray()
    }
    fun buildWTLoginCheckSMS(): ByteArray {
        val finalResult = Buffer()
        finalResult.writeShort(7)
        finalResult.writeShort(tlvCount.toShort()) // 先写总数
        finalResult.writeFully(mainBuffer.readByteArray()) // 再写所有内容
        return finalResult.readByteArray()
    }
    fun build(): ByteArray{
        val finalResult = Buffer()
        finalResult.writeShort(tlvCount.toShort()) // 先写总数
        finalResult.writeFully(mainBuffer.readByteArray()) // 再写所有内容
        return finalResult.readByteArray()
    }
}