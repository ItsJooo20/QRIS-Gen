package com.example.qrisgen.utils

object CRCCalculator {
    fun calculateCRC(payload: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        payload.toByteArray().forEach { byte ->
            var temp = (byte.toInt() and 0xFF) shl 8
            crc = crc xor temp

            repeat(8) {
                crc = if ((crc and 0x8000) != 0) {
                    (crc shl 1) xor polynomial
                } else {
                    crc shl 1
                }
            }
        }

        return String.format("%04X", crc and 0xFFFF)
    }

    fun addCRCToPayload(payload: String): String {
        val payloadWithCRCTag = payload + Constants.EMVTags.CRC + "04"

        val crc = calculateCRC(payloadWithCRCTag)

        return payloadWithCRCTag + crc
    }
}