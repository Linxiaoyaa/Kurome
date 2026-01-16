package com.kurome.app
import com.kurome.app.botsetting.createNewBot
import com.kurome.app.internal.service.system.getLogin
import com.kurome.app.socket.BotClient
import com.kurome.app.utils.*


suspend fun main() {
    val bot = createNewBot(22262852,"chunx02.",getRandomBytes(16))
    println(bot.appinfo.qua)
    println(bot.keystore.password2Key.toHexString())

    val client = BotClient("msfwifi.3g.qq.com",8080)
    client.connect()
    val bin = getLogin(bot)
    println(bin.toHexString())
    client.send(bin)

}
