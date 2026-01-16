package com.kurome.app.utils


import java.security.MessageDigest
import java.security.SecureRandom
import java.util.HexFormat
fun md5(input: ByteArray): ByteArray {
    return MessageDigest.getInstance("MD5").digest(input)
}

// 获取随机字节

fun getRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    SecureRandom().nextBytes(bytes)
    return bytes
}



fun getRandomString(length: Int = 16): String {
    val charPool = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = SecureRandom()
    val sb = StringBuilder(length)

    repeat(length) {
        val index = random.nextInt(charPool.length)
        sb.append(charPool[index])
    }

    return sb.toString()
}

