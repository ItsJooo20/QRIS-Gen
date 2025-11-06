package com.example.qrisgen.utils

import com.example.qrisgen.data.model.MerchantInfo
import com.example.qrisgen.data.model.ParsedQRIS

object QRISParser {

    fun parseQRIS(payload: String): ParsedQRIS {
        try {
            val tlvMap = EMVUtils.parseTLV(payload)

            val merchantAccountData = tlvMap[Constants.EMVTags.MERCHANT_ACCOUNT] ?: ""
            val merchantAccountMap = if (merchantAccountData.isNotEmpty()) {
                EMVUtils.parseTLV(merchantAccountData)
            } else {
                emptyMap()
            }

            val merchantInfo = MerchantInfo(
                name = tlvMap[Constants.EMVTags.MERCHANT_NAME]
                    ?: Constants.DefaultMerchant.NAME,
                city = tlvMap[Constants.EMVTags.MERCHANT_CITY]
                    ?: Constants.DefaultMerchant.CITY,
                id = merchantAccountMap[Constants.EMVTags.MerchantAccount.MERCHANT_PAN],
                category = tlvMap[Constants.EMVTags.MERCHANT_CATEGORY],
                acquirer = merchantAccountMap[Constants.EMVTags.MerchantAccount.GLOBAL_UNIQUE_ID],
                postCode = tlvMap[Constants.EMVTags.POSTAL_CODE],
            )

            val amountString = tlvMap[Constants.EMVTags.TRANSACTION_AMOUNT]
            val amount = amountString?.toDoubleOrNull()?.toLong() ?: 0L

            val initiationMethod = tlvMap[Constants.EMVTags.POINT_OF_INITIATION] ?: "11"

            return ParsedQRIS(
                isValid = true,
                merchant = merchantInfo,
                amount = amount,
                currencyCode = tlvMap[Constants.EMVTags.TRANSACTION_CURRENCY]
                    ?: Constants.DefaultMerchant.CURRENCY_CODE,
                countryCode = tlvMap[Constants.EMVTags.COUNTRY_CODE]
                    ?: Constants.DefaultMerchant.COUNTRY,
                initiationMethod = initiationMethod,
                originalPayload = payload,
                tlvMap = tlvMap
            )

        } catch (e: Exception) {
            return ParsedQRIS(
                isValid = false,
                merchant = MerchantInfo(
                    name = Constants.DefaultMerchant.NAME,
                    city = Constants.DefaultMerchant.CITY
                ),
                amount = 0L,
                originalPayload = payload,
                errorMessage = e.message
            )
        }
    }

    fun isValidQRIS(payload: String): Boolean {
        val cleanPayload = payload.trim()
        println("Payload length: ${cleanPayload.length}")

        if (cleanPayload.length < 50) {
            println("Too short")
            return false
        }

        try {
            if (!cleanPayload.contains("6304")) {
                println("No CRC tag (6304) found")
                return false
            }

            val providedCRC = cleanPayload.takeLast(4).uppercase()
            val payloadForCalculation = cleanPayload.substring(0, cleanPayload.length - 4)

            println("Payload for calc: $payloadForCalculation")
            println("Provided CRC: $providedCRC")

            val calculatedCRC = CRCCalculator.calculateCRC(payloadForCalculation).uppercase()

            println("Calculated CRC: $calculatedCRC")
            println("Match: ${providedCRC == calculatedCRC}")

            return providedCRC == calculatedCRC

        } catch (e: Exception) {
            println("Exception: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}