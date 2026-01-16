package com.kurome.app.socket

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.MultiThreadIoEventLoopGroup
import io.netty.channel.nio.NioIoHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class BotClient(val host: String, val port: Int) {
    private val group = MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory())
    private var channel: Channel? = null


    private var responsePromise: CompletableFuture<ByteArray>? = null

    fun connect() {
        val b = Bootstrap()
        b.group(group)
            .channel(NioSocketChannel::class.java)
            .remoteAddress(InetSocketAddress(host, port))
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    val p = ch.pipeline()
                    p.addLast(io.netty.handler.codec.LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, -4, 0
                    ))
                    p.addLast(BotClientHandler(this@BotClient))
                }
            })

        val f = b.connect().sync()
        this.channel = f.channel()
    }


    fun send(data: ByteArray, timeout: Long = 10): ByteArray? {
        val ch = channel
        if (ch == null || !ch.isActive) return null


        val promise = CompletableFuture<ByteArray>()
        this.responsePromise = promise


        val buf = Unpooled.copiedBuffer(data)
        ch.writeAndFlush(buf)

        return try {
            promise.get(timeout, TimeUnit.SECONDS)
        } catch (e: Exception) {
            println("Send Timeout or Error: ${e.message}")
            null
        } finally {
            this.responsePromise = null
        }
    }

    fun fulfillPromise(data: ByteArray) {
        responsePromise?.complete(data)
    }

    fun close() {
        group.shutdownGracefully()
        println("Closed")
    }
}

class BotClientHandler(private val client: BotClient) : SimpleChannelInboundHandler<ByteBuf>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        val length = msg.readableBytes()
        val data = ByteArray(length)
        msg.readBytes(data)

        client.fulfillPromise(data)
    }
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        println("Internal Error: ${cause.message}")
        ctx.close()
    }
}