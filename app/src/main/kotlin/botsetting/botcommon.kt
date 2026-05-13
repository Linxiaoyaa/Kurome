@file:UseSerializers(ByteArrayHexSerializer::class)
package botsetting


import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import utils.crypto.ecdh.generateEcdhV2
import utils.getRandomBytes
import utils.md5
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}
object BotManager {
    private val activeBots = ConcurrentHashMap<Long, BotCommon>()
    private val deviceDir = File("accounts")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    init {
        if (!deviceDir.exists()) deviceDir.mkdirs()
    }
   fun registerBot(bot: BotCommon): BotCommon {
        val uin = bot.keystore.uin
        bot.initClient("msfwifi.3g.qq.com", 8080)
        bot.client.onDispatchPacket = { data ->
            bot.keystore.MsgCookies = data.msgCookies
            logger.info { "cmd: ${data.cmd} buffer: ${data.body.toHexString()}" }
        }
//       AppScope.botScope.launch {
//            try {
//                val isConnected = bot.client.connect()
//                if (isConnected) {
//                    activeBots[uin] = bot
//                } else {
//                    logger.error { "Connected Server Failed" }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

        activeBots[uin] = bot
        return bot
    }
    fun saveAccount(uin: Long) {
        val bot = activeBots[uin] ?: return
        try {
            val file = File(deviceDir, "$uin.json")
            val data = json.encodeToString(bot)
            file.writeText(data)
            logger.info { "$uin saved to ${file.absolutePath}" }
        } catch (e: Exception) {
            logger.error(e) { "save $uin failed" }
        }
    }
    fun getBot(uin: Long): BotCommon? = activeBots[uin]
    fun getCount(): Int = activeBots.size
    fun addAccount(uin: Long, password: String, guid: ByteArray): BotCommon {
        if (activeBots.containsKey(uin)) {
            logger.warn { "$uin is already registered, adding as a new account" }
        }

        logger.info { "adding new account : $uin with password $password" }

        val bot = createNewBot(uin, password, guid)

        activeBots[uin] = bot
        return bot
    }
    fun loadAllSavedAccounts() {
        val files = deviceDir.listFiles { _, name -> name.endsWith(".json") } ?: return
        files.forEach { file ->
            try {
                val content = file.readText()
                val bot = json.decodeFromString<BotCommon>(content)

                registerBot(bot)
                logger.info { "recovery : ${bot.keystore.uin}" }
            } catch (e: Exception) {
                logger.error(e) { "recovery ${file.name} failed" }
            }
        }
    }
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
        keyStore.WLoginSigs.TGTGTKey = getRandomBytes(16)
        keyStore.ECDH = generateEcdhV2()
        keyStore.uin = uin
        keyStore.guid = guid.toHexString()
        val bot = BotCommon(keyStore, appInfo, BotLoginInfo())
        return registerBot(bot)
    }
}