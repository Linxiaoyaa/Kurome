package utils.crypto.tea


import kotlin.random.Random

object TeaProvider {

    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        if (key.size != 16) return byteArrayOf()
        return TEAImpl().encrypt(data, key)
    }

    fun decrypt(data: ByteArray, key: ByteArray): ByteArray? {
        if (data.size < 16 || data.size % 8 != 0 || key.size != 16) return null
        return try {
            TEAImpl().decrypt(data, key)
        } catch (e: Exception) {
            null
        }
    }
}

private class TEAImpl {
    private var plain = ByteArray(8)
    private var prePlain = ByteArray(8)
    private var out = byteArrayOf()
    private var crypt = 0
    private var preCrypt = 0
    private var pos = 0
    private var padding = 0
    private var key = ByteArray(16)
    private var header = true
    private var contextStart = 0


    private fun encipher(input: ByteArray, offset: Int): ByteArray {
        var y = getInt(input, offset)
        var z = getInt(input, offset + 4)
        val k0 = getInt(key, 0)
        val k1 = getInt(key, 4)
        val k2 = getInt(key, 8)
        val k3 = getInt(key, 12)
        var sum = 0
        val delta = -0x61c88647 // 0x9E3779B9

        repeat(16) {
            sum += delta
            y += ((z shl 4) + k0) xor (z + sum) xor ((z ushr 5) + k1)
            z += ((y shl 4) + k2) xor (y + sum) xor ((y ushr 5) + k3)
        }

        val res = ByteArray(8)
        writeInt(res, 0, y)
        writeInt(res, 4, z)
        return res
    }

    private fun decipher(input: ByteArray, offset: Int): ByteArray {
        var y = getInt(input, offset)
        var z = getInt(input, offset + 4)
        val k0 = getInt(key, 0)
        val k1 = getInt(key, 4)
        val k2 = getInt(key, 8)
        val k3 = getInt(key, 12)
        val delta = -0x61c88647
        var sum = delta shl 4

        repeat(16) {
            z -= ((y shl 4) + k2) xor (y + sum) xor ((y ushr 5) + k3)
            y -= ((z shl 4) + k0) xor (z + sum) xor ((z ushr 5) + k1)
            sum -= delta
        }

        val res = ByteArray(8)
        writeInt(res, 0, y)
        writeInt(res, 4, z)
        return res
    }

    fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
        this.key = key
        val len = data.size
        pos = (len + 10) % 8
        if (pos != 0) pos = 8 - pos

        out = ByteArray(len + pos + 10)
        plain[0] = ((Random.nextInt() and 0xF8) or pos).toByte()

        for (i in 1..pos) plain[i] = (Random.nextInt() and 0xFF).toByte()
        pos++

        padding = 1
        while (padding <= 2) {
            if (pos < 8) {
                plain[pos++] = (Random.nextInt() and 0xFF).toByte()
                padding++
            }
            if (pos == 8) encrypt8Bytes()
        }

        var dPos = 0
        var remain = len
        while (remain > 0) {
            if (pos < 8) {
                plain[pos++] = data[dPos++]
                remain--
            }
            if (pos == 8) encrypt8Bytes()
        }

        padding = 1
        while (padding <= 7) {
            if (pos < 8) {
                plain[pos++] = 0
                padding++
            }
            if (pos == 8) encrypt8Bytes()
        }
        return out
    }

    private fun encrypt8Bytes() {
        pos = 0
        while (pos < 8) {
            if (header) {
                plain[pos] = (plain[pos].toInt() xor prePlain[pos].toInt()).toByte()
            } else {
                plain[pos] = (plain[pos].toInt() xor out[preCrypt + pos].toInt()).toByte()
            }
            pos++
        }

        val encrypted = encipher(plain, 0)
        System.arraycopy(encrypted, 0, out, crypt, 8)

        pos = 0
        while (pos < 8) {
            out[crypt + pos] = (out[crypt + pos].toInt() xor prePlain[pos].toInt()).toByte()
            pos++
        }

        System.arraycopy(plain, 0, prePlain, 0, 8)
        preCrypt = crypt
        crypt += 8
        pos = 0
        header = false
    }

    fun decrypt(data: ByteArray, key: ByteArray): ByteArray? {
        this.key = key
        val len = data.size
        val deciphered = decipher(data, 0)
        prePlain = deciphered
        pos = deciphered[0].toInt() and 7
        val outLen = len - pos - 10
        if (outLen < 0) return null

        val res = ByteArray(outLen)
        out = res
        preCrypt = 0
        crypt = 8
        contextStart = 8
        pos++

        padding = 1
        while (padding <= 2) {
            if (pos < 8) {
                pos++
                padding++
            }
            if (pos == 8) {
                if (!decrypt8Bytes(data)) return null
            }
        }

        var resPos = 0
        var remain = outLen
        while (remain > 0) {
            if (pos < 8) {
                res[resPos++] = (data[crypt + pos] xor prePlain[pos])
                remain--
                pos++
            }
            if (pos == 8) {
                preCrypt = crypt
                if (!decrypt8Bytes(data)) return null
            }
        }

        padding = 1
        while (padding < 8) {
            if (pos < 8) {
                if ((data[crypt + pos] xor prePlain[pos]).toInt() != 0) return null
                pos++
            }
            if (pos == 8) {
                preCrypt = crypt
                if (!decrypt8Bytes(data)) return null
            }
            padding++
        }
        return res
    }

    private fun decrypt8Bytes(data: ByteArray): Boolean {
        pos = 0
        while (pos < 8) {
            if (contextStart + pos >= data.size) return true
            prePlain[pos] = (prePlain[pos].toInt() xor data[crypt + pos].toInt()).toByte()
            pos++
        }
        prePlain = decipher(prePlain, 0)
        contextStart += 8
        crypt += 8
        pos = 0
        return true
    }

    private fun getInt(b: ByteArray, off: Int): Int {
        return (b[off].toInt() shl 24) or
                ((b[off + 1].toInt() and 0xFF) shl 16) or
                ((b[off + 2].toInt() and 0xFF) shl 8) or
                (b[off + 3].toInt() and 0xFF)
    }

    private fun writeInt(b: ByteArray, off: Int, v: Int) {
        b[off] = (v ushr 24).toByte()
        b[off + 1] = (v ushr 16).toByte()
        b[off + 2] = (v ushr 8).toByte()
        b[off + 3] = v.toByte()
    }

    private infix fun Byte.xor(other: Byte): Byte = (this.toInt() xor other.toInt()).toByte()
}