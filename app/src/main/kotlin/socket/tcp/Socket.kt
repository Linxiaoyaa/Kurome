package socket.tcp

import botsetting.BotCommon
import internal.packet.system.BotNetworkManager
import internal.packet.system.Packet
import internal.packet.system.decodeHeader
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class BotClient(val host: String, val port: Int, val bot: BotCommon) {
    private var channel: Channel? = null
    var onDispatchPacket: ((Packet) -> Unit)? = null
    private val responsePromises = ConcurrentHashMap<Int, CompletableFuture<ByteArray>>()

    suspend fun connect(): Boolean = suspendCancellableCoroutine { continuation ->

        val b = Bootstrap()
            .group(BotNetworkManager.group)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.attr(BotNetworkManager.CLIENT_KEY).set(this@BotClient)
                    ch.pipeline().addLast(
                        LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, -4, 0),
                        BotNetworkManager.sharedHandler
                    )
                }
            })
        b.connect(host, port).addListener { future ->
            if (future.isSuccess) {
                val f = future as io.netty.channel.ChannelFuture
                this.channel = f.channel()
                this.channel?.closeFuture()?.addListener { onDisconnect() }
                bot.log.info { "${bot.keystore.uin} 连接成功" }
                if (continuation.isActive) continuation.resume(true)
            } else {
                bot.log.error { "${bot.keystore.uin} 连接失败: ${future.cause()?.message}" }
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }
    fun disconnect() {
        val ch = channel
        if (ch != null && ch.isActive) {
            ch.close().addListener { future ->
                if (future.isSuccess) {
                    bot.log.info { "Disconnected from server" }
                }
            }
        }
        channel = null
        onDisconnect()
    }
   suspend fun send(seq: Int, data: ByteArray, timeout: Long = 5000): ByteArray? {
        val ch = channel ?: return null
        if (!ch.isActive) return null

        val promise = CompletableFuture<ByteArray>()
        responsePromises[seq] = promise
        ch.writeAndFlush(Unpooled.copiedBuffer(data))


        return try {
            withTimeoutOrNull(timeout.milliseconds) {
                promise.await()
            }
        } catch (e: Exception) {
            bot.log.error(e) { "[${bot.keystore.uin}] Request $seq Error/Timeout: ${e.message}" }
            null
        } finally {
            responsePromises.remove(seq)
        }
    }


    fun handleIncomingData(data: ByteArray) {
        val result = decodeHeader(data, bot)
        val seq = result.seq
        val body = result.body
        bot.log.debug { result.body.toHexString()+" "+result.seq }

        val promise = responsePromises[seq]
        if (promise != null) {

            promise.complete(body)
        } else {

            onDispatchPacket?.invoke(result)
        }
    }

    fun onDisconnect() {
        responsePromises.values.forEach { it.completeExceptionally(RuntimeException("Connection lost")) }
        responsePromises.clear()
    }
}

@ChannelHandler.Sharable
class BotClientHandler : SimpleChannelInboundHandler<ByteBuf>() {

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        val client = ctx.channel().attr(BotNetworkManager.CLIENT_KEY).get() ?: return
        val data = ByteArray(msg.readableBytes())
        msg.readBytes(data)
        client.handleIncomingData(data)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val client = ctx.channel().attr(BotNetworkManager.CLIENT_KEY).get()
        client?.onDisconnect()
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        val client = ctx.channel().attr(BotNetworkManager.CLIENT_KEY).get()
        val log = client?.bot?.log
        log?.error(cause) {  "[${client.bot.keystore.uin}] Network Error: ${cause.message}"}
        ctx.close()
    }
}