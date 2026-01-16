package com.kurome.app
import com.kurome.app.botsetting.createNewBot
import com.kurome.app.internal.service.system.wtLogin
import com.kurome.app.utils.*


fun main() {
    val bot = createNewBot(22262852,"chunx02.",getRandomBytes(16))
    wtLogin(bot)

}
