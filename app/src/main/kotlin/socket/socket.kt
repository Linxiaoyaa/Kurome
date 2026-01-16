package com.kurome.app.socket

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler


class BotClient(val host: String, val port: Int) {
    private val group = NioEventLoopGroup()
    private var channel: Channel? = null

    // 启动连接（只在程序开始时调用一次）
    fun connect() {
        val b = Bootstrap()
        b.group(group)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(InetSocketAddress(host, port))
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    // 强烈建议在这里加上拆包器，否则长连接多次收到包会粘在一起
                    // ch.pipeline().addLast(LengthFieldBasedFrameDecoder(...))
                    ch.pipeline().addLast(BotClientHandler())
                }
            })

        val f = b.connect().sync() // 阻塞直到连接成功
        this.channel = f.channel()
        println("Already Connect :${host}:${port}")
    }

    // 随时调用的发包函数
    fun send(data: ByteArray) {
        val ch = channel
        if (ch != null && ch.isActive) {
            val buf = Unpooled.copiedBuffer(data)
            ch.writeAndFlush(buf)

        } else {
            println("Send Filed")
        }
    }

    // 关闭连接（程序退出时调用）
    fun close() {
        group.shutdownGracefully()
        println("Close")
    }
}

class BotClientHandler : SimpleChannelInboundHandler<ByteBuf>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        val data = ByteArray(msg.readableBytes())
        msg.readBytes(data)

        // 这里是你处理所有返回包的地方
        // 在正式项目中，你通常会在这里根据包头的命令号，分发给不同的处理函数
        println("Recive [${data.size} bytes]: ${data.toHexString()}")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        println("Server Already Close Connection")
        // 这里可以实现自动重连逻辑
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("Internal Bad : ${cause.message}")
        ctx.close()
    }
}