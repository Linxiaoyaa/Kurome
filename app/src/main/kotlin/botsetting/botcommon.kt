@file:UseSerializers(ByteArrayHexSerializer::class)
package botsetting


import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import internal.packet.system.readFuncBuffer
import internal.service.store.ByteArrayHexSerializer
import kotlinx.serialization.UseSerializers
import utils.md5
import utils.crypto.ecdh.generateEcdhV2
import utils.getRandomBytes


fun createNewBot(uin: Long, password: String, guid: ByteArray): BotCommon {
    val appInfo = BotAppinfo()
    val keyStore = BotKeystore(
        ECDH = generateEcdhV2()
    )

    val passwordBytes = password.toByteArray()
    val pwMd5 = md5(passwordBytes)
    val baos = ByteArrayOutputStream()
    val dos = DataOutputStream(baos)
    dos.write(pwMd5)
    dos.writeInt(0)
    dos.writeInt(uin.toInt())

    keyStore.password = password
    keyStore.password2Key = md5(baos.toByteArray())
    keyStore.passwordKey = pwMd5
    println("${keyStore.password},${keyStore.passwordKey.toHexString()},${keyStore.password2Key.toHexString()}")
    keyStore.WLoginSigs.TGTGTKey = getRandomBytes(16)
    keyStore.ECDH = generateEcdhV2()
    keyStore.uin = uin
    keyStore.guid = guid.toHexString()
    return BotCommon(
         keyStore,appInfo
    ).apply {
        client.bot = this
        client.onDispatchPacket = { data ->
            readFuncBuffer(data)
        }
    }
}