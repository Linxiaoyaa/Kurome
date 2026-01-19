package utils.crypto.qrsig

object WtloginTrans {
    private val DECODE_TABLE = IntArray(128) { -1 }
    init {
        for (c in 'A'..'Z') DECODE_TABLE[c.code] = c.code - 'A'.code
        for (c in 'a'..'z') DECODE_TABLE[c.code] = c.code - 'a'.code + 26
        for (c in '0'..'9') DECODE_TABLE[c.code] = c.code - '0'.code + 52
        DECODE_TABLE['*'.code] = 62
        DECODE_TABLE['-'.code] = 63
        DECODE_TABLE[' '.code] = 62
    }
    fun decode(input: ByteArray, length: Int): ByteArray {
        val output = ByteArray(24)
        var outIdx = 0
        var validCount = 0
        for (k in 0 until length) {
            if (k >= input.size) break
            val charCode = input[k].toInt() and 0xFF
            if (charCode == 95) {
                return output
            }
            if (charCode >= DECODE_TABLE.size) continue
            val val6Bit = DECODE_TABLE[charCode]
            if (val6Bit < 0) {
                continue
            }
            val remainder = validCount % 4
            when (remainder) {
                0 -> {

                    output[outIdx] = (val6Bit shl 2).toByte()

                }
                1 -> {

                    output[outIdx] = (output[outIdx].toInt() or (val6Bit shr 4)).toByte()

                    outIdx++


                    if (outIdx < output.size) {
                        output[outIdx] = ((val6Bit and 0x0F) shl 4).toByte()
                    }
                }
                2 -> {

                    output[outIdx] = (output[outIdx].toInt() or (val6Bit shr 2)).toByte()

                    outIdx++


                    if (outIdx < output.size) {
                        output[outIdx] = ((val6Bit and 0x03) shl 6).toByte()
                    }
                }
                3 -> {
                    output[outIdx] = (output[outIdx].toInt() or val6Bit).toByte()

                    outIdx++
                }
            }

            validCount++
        }
        return output
    }
}