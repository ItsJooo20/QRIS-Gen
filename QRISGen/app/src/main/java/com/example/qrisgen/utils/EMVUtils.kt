package com.example.qrisgen.utils

object EMVUtils {
    fun buildTLV(tag: String, value: String): String {
        if (value.isEmpty()) return ""

        val length = value.length.toString().padStart(2, '0')
        return tag + length + value
    }

    fun parseTLV(payload: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var index = 0

        while (index < payload.length - 4) {
            try {
                val tag = payload.substring(index, index + 2)

                val length = payload.substring(index + 2, index + 4).toInt()

                if (index + 4 + length <= payload.length) {
                    val value = payload.substring(index + 4, index + 4 + length)
                    result[tag] = value

                    index += 4 + length
                } else {
                    break
                }
            } catch (e: Exception) {
                break
            }
        }

        return result
    }
}