import botsetting.WLoginSigs
import botsetting.createNewBot
import internal.packet.state.online
import internal.service.store.SessionManager
import internal.service.system.wtLogin
import utils.crypto.qrsig.WtloginTrans
import utils.getRandomBytes

fun main() {
    /*
    val uin =22262852L
    val bot = createNewBot(uin, "chunx02.", getRandomBytes(16))
    println("sharekey:${bot.keystore.ECDH.shareKey.toHexString()},tgtgtKey:${bot.keystore.WLoginSigs.TGTGTKey.toHexString()}")
    //wtLogin(bot)
    val hasSession = SessionManager.loadSigsIntoBot(uin, bot)

    if (hasSession) {
        println("发现本地缓存，尝试执行 D2 快速登录...")
        bot.client.connect()
        online(bot)
    } else {
        println("无缓存，执行完整登录流程")
        wtLogin(bot)
    }

     */
    println(WtloginTrans.decode("WlXsja-JJp*eJ2P7tWgNen8KlxR1mIdb".toByteArray(),32).toHexString())
}
