package com.kurome.app.botsetting


import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import botsetting.*
import com.kurome.app.utils.md5
import com.kurome.app.utils.*
import com.kurome.app.utils.crypto.ecdh.generateEcdhV2


fun createNewBot(uin: Long, password: String, guid: ByteArray): BotCommon {
    val appInfo = BotAppinfo()
    val keyStore = BotKeystore(
        ECDH = generateEcdhV2()
    )

    val passwordBytes = password.toByteArray(Charsets.UTF_8)
    val pwMd5 = md5(passwordBytes)

    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)

    dos.write(pwMd5)
    dos.writeInt(0)
    dos.writeInt(uin.toInt())


    keyStore.password = password
    keyStore.password2Key = md5(baos.toByteArray())
    keyStore.passwordKey = pwMd5
    keyStore.TGTGTKey = getRandomBytes(16)
    keyStore.ECDH = generateEcdhV2()
    return BotCommon(
         keyStore,appInfo
    )
}