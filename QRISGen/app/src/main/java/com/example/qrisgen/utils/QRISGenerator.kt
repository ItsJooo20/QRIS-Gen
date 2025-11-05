package com.example.qrisgen.utils

import com.example.qrisgen.data.model.ParsedQRIS

object QRISGenerator {

    fun generateDynamicQRIS(
        staticPayload: String,
        amount: Long,
        feeType: String = Constants.FeeType.NONE,
        feeValue: Double = 0.0,
        customMerchantName: String? = null,
        customMerchantCity: String? = null,
        customMerchantPostCode: String? = null,
    ): String {
        val parsed = QRISParser.parseQRIS(staticPayload)

        if (!parsed.isValid) {
            throw IllegalArgumentException("Invalid QRIS payload")
        }

        val finalAmount = when (feeType) {
            Constants.FeeType.FIXED -> amount + feeValue.toLong()
            Constants.FeeType.PERCENTAGE -> amount + (amount * feeValue / 100).toLong()
            else -> amount
        }

        println("Generating QRIS with amount: $finalAmount")
        if (customMerchantName != null) {
            println("Custom Merchant: $customMerchantName")
        }
        if (customMerchantCity != null) {
            println("Custom City: $customMerchantCity")
        }
        if (customMerchantPostCode != null) {
            println("Custom Post Code: $customMerchantPostCode")
        }

        val generatedPayload = buildQRISPayload(
            parsed = parsed,
            amount = finalAmount,
            isDynamic = true,
            customMerchantName = customMerchantName,
            customMerchantCity = customMerchantCity,
            customMerchantPostCode = customMerchantPostCode
        )
        return generatedPayload
    }

    private fun buildQRISPayload(
        parsed: ParsedQRIS,
        amount: Long,
        isDynamic: Boolean,
        customMerchantName: String? = null,
        customMerchantCity: String? = null,
//        customMerchantCategory: String? = null,
        customMerchantPostCode: String? = null,
    ): String {
        var payload = ""

        val tlvMap = parsed.tlvMap

        payload += EMVUtils.buildTLV(Constants.EMVTags.PAYLOAD_FORMAT, "01")

        val initiationMethod = if (isDynamic) {
            Constants.InitiationMethod.DYNAMIC
        } else {
            Constants.InitiationMethod.STATIC
        }
        payload += EMVUtils.buildTLV(Constants.EMVTags.POINT_OF_INITIATION, initiationMethod)

        val merchantAccount = tlvMap[Constants.EMVTags.MERCHANT_ACCOUNT]
        if (!merchantAccount.isNullOrEmpty()) {
            payload += EMVUtils.buildTLV(Constants.EMVTags.MERCHANT_ACCOUNT, merchantAccount)
        }

        tlvMap["51"]?.let { tag51 ->
            payload += EMVUtils.buildTLV("51", tag51)
        }

        tlvMap[Constants.EMVTags.MERCHANT_CATEGORY]?.let { category ->
            payload += EMVUtils.buildTLV(Constants.EMVTags.MERCHANT_CATEGORY, category)
        }

        payload += EMVUtils.buildTLV(
            Constants.EMVTags.TRANSACTION_CURRENCY,
            tlvMap[Constants.EMVTags.TRANSACTION_CURRENCY] ?: "360"
        )

        if (isDynamic && amount > 0) {
            val amountString = String.format("%.2f", amount.toDouble())
            payload += EMVUtils.buildTLV(Constants.EMVTags.TRANSACTION_AMOUNT, amountString)
        }

        payload += EMVUtils.buildTLV(
            Constants.EMVTags.COUNTRY_CODE,
            tlvMap[Constants.EMVTags.COUNTRY_CODE] ?: "ID"
        )

        val merchantName = customMerchantName ?: parsed.merchant.name
        payload += EMVUtils.buildTLV(Constants.EMVTags.MERCHANT_NAME, merchantName)
        val merchantCity = customMerchantCity ?: parsed.merchant.city
        payload += EMVUtils.buildTLV(Constants.EMVTags.MERCHANT_CITY, merchantCity)
        val merchantPostCode = customMerchantPostCode ?: parsed.merchant.postCode
        payload += EMVUtils.buildTLV(Constants.EMVTags.POSTAL_CODE, merchantPostCode?:"")

        tlvMap[Constants.EMVTags.ADDITIONAL_DATA]?.let { additionalData ->
            payload += EMVUtils.buildTLV(Constants.EMVTags.ADDITIONAL_DATA, additionalData)
        }
        return CRCCalculator.addCRCToPayload(payload)
    }
}