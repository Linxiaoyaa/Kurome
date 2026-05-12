package internal.packet.system

import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.util.AttributeKey
import socket.tcp.BotClient
import socket.tcp.BotClientHandler

object BotNetworkManager {

    val group = MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, NioIoHandler.newFactory())

    val CLIENT_KEY: AttributeKey<BotClient?>? = AttributeKey.valueOf<BotClient>("BotClient")

    fun shutdown() {
        group.shutdownGracefully()
    }
    val sharedHandler = BotClientHandler()
}