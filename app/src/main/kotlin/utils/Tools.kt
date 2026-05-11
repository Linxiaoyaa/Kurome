package utils


import java.security.MessageDigest
import java.security.SecureRandom

fun md5(input: ByteArray): ByteArray {
    return MessageDigest.getInstance("MD5").digest(input)
}


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

