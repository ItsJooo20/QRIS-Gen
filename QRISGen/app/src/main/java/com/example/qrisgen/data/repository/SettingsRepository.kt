package com.example.qrisgen.data.repository

import android.content.Context
import com.example.qrisgen.data.model.MerchantInfo
import com.example.qrisgen.utils.Constants
import com.example.qrisgen.utils.PreferencesManager

class SettingsRepository(private val context: Context) {
    fun saveQRISPayload(payload: String) {
        PreferencesManager.saveQRISPayload(context, payload)
    }

    fun getQRISPayload(): String? {
        return PreferencesManager.getQRISPayload(context)
    }

    fun clearQRISPayload() {
        PreferencesManager.clearQRISPayload(context)
    }

    fun isQRISConfigured(): Boolean {
        return PreferencesManager.isQRISConfigured(context)
    }

    fun saveMerchantInfo(
        name: String,
        city: String,
        category: String?,
        postalCode: String?
    ) {
        PreferencesManager.saveMerchantInfo(
            context = context,
            name = name,
            city = city,
            category = category,
            postalCode = postalCode
        )
    }

    fun getMerchantInfo(): MerchantInfo {
        return MerchantInfo(
            name = PreferencesManager.getMerchantName(context),
            city = PreferencesManager.getMerchantCity(context),
            category = PreferencesManager.getMerchantCategory(context),
            postCode = PreferencesManager.getPostalCode(context)
        )
    }

    fun getMerchantName(): String {
        return PreferencesManager.getMerchantName(context)
    }

    fun getMerchantCity(): String {
        return PreferencesManager.getMerchantCity(context)
    }

    fun saveFeeSettings(feeType: String, feeValue: Double) {
        PreferencesManager.saveFeeSettings(context, feeType, feeValue)
    }

    fun getFeeSettings(): Pair<String, Double> {
        val feeType = PreferencesManager.getFeeType(context)
        val feeValue = PreferencesManager.getFeeValue(context)
        return Pair(feeType, feeValue)
    }

    fun getFeeType(): String {
        return PreferencesManager.getFeeType(context)
    }

    fun getFeeValue(): Double {
        return PreferencesManager.getFeeValue(context)
    }

    data class CompleteSettings(
        val qrisPayload: String,
        val merchantInfo: MerchantInfo,
        val feeType: String,
        val feeValue: Double
    )

    fun loadCompleteSettings(): CompleteSettings? {
        val payload = getQRISPayload() ?: return null

        return CompleteSettings(
            qrisPayload = payload,
            merchantInfo = getMerchantInfo(),
            feeType = getFeeType(),
            feeValue = getFeeValue()
        )
    }

    fun saveCompleteSettings(
        payload: String,
        merchantName: String,
        merchantCity: String,
        merchantCategory: String?,
        postalCode: String?,
        feeType: String,
        feeValue: Double
    ) {
        saveQRISPayload(payload)
        saveMerchantInfo(merchantName, merchantCity, merchantCategory, postalCode)
        saveFeeSettings(feeType, feeValue)
    }
}