package com.example.qrisgen.data.model

import com.example.qrisgen.utils.Constants

data class QRISTransaction(
    val id: Long = 0,
    val originalPayload: String,
    val generatedPayload: String? = null,
    val amount: Long,
    val feeType: String = Constants.FeeType.NONE,
    val feeValue: Double = 0.0,
    val merchant: MerchantInfo,
    val timestamp: Long = System.currentTimeMillis()
) {
    val finalAmount: Long
        get() = when (feeType) {
            Constants.FeeType.FIXED -> amount + feeValue.toLong()
            Constants.FeeType.PERCENTAGE -> amount + (amount * feeValue / 100).toLong()
            else -> amount
        }
}

data class MerchantInfo(
    val name: String,
    val city: String,
    val id: String? = null,
    val category: String? = null,
    val acquirer: String? = null,
    val postCode: String? = null,
)

data class ParsedQRIS(
    val isValid: Boolean,
    val merchant: MerchantInfo,
    val amount: Long,
    val currencyCode: String = Constants.DefaultMerchant.CURRENCY_CODE,
    val countryCode: String = Constants.DefaultMerchant.COUNTRY,
    val initiationMethod: String = "11",
    val originalPayload: String,
    val tlvMap: Map<String, String> = emptyMap(),
    val errorMessage: String? = null
)