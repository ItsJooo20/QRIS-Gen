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
        if (payload.length < 50) return false

        try {
            if (!payload.contains("6304")) {
                println("No CRC tag found")
                return false
            }

            val payloadWithoutCRC = payload.substring(0, payload.length - 4)
            val providedCRC = payload.substring(payload.length - 4)

            val calculatedCRC = CRCCalculator.calculateCRC(
                payloadWithoutCRC + Constants.EMVTags.CRC + "04"
            )

            return providedCRC.equals(calculatedCRC, ignoreCase = true)

        } catch (e: Exception) {
            return false
        }
    }
}