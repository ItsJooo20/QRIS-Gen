package com.example.qrisgen.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {

    private const val PREF_NAME = "qris_gen_prefs"

    object Keys {
        const val QRIS_PAYLOAD = "qris_payload"

        const val MERCHANT_NAME = "merchant_name"
        const val MERCHANT_CITY = "merchant_city"
        const val MERCHANT_CATEGORY = "merchant_category"
        const val MERCHANT_ID = "merchant_id"
        const val MERCHANT_ACQUIRER = "merchant_acquirer"

        const val COUNTRY_CODE = "country_code"
        const val CURRENCY_CODE = "currency_code"
        const val POSTAL_CODE = "postal_code"

        const val FEE_TYPE = "fee_type"
        const val FEE_VALUE = "fee_value"

        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val IS_LOGGED_IN = "is_logged_in"

        const val FIRST_TIME = "first_time"
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isQRISConfigured(context: Context): Boolean {
        return getQRISPayload(context) != null
    }

    fun saveQRISPayload(context: Context, payload: String) {
        getPrefs(context).edit().putString(Keys.QRIS_PAYLOAD, payload).apply()
        println("QRIS payload saved")
    }

    fun getQRISPayload(context: Context): String? {
        return getPrefs(context).getString(Keys.QRIS_PAYLOAD, null)
    }

    fun clearQRISPayload(context: Context) {
        getPrefs(context).edit().remove(Keys.QRIS_PAYLOAD).apply()
        println("QRIS payload cleared")
    }

    fun saveMerchantInfo(
        context: Context,
        name: String,
        city: String,
        postalCode: String? = null,
        category: String? = null,
        id: String? = null,
        acquirer: String? = null
    ) {
        getPrefs(context).edit().apply {
            putString(Keys.MERCHANT_NAME, name)
            putString(Keys.MERCHANT_CITY, city)
            if (postalCode.isNullOrEmpty()) {
                remove(Keys.POSTAL_CODE)
            } else {
                putString(Keys.POSTAL_CODE, postalCode)
            }
            putString(Keys.MERCHANT_CATEGORY, category)
            putString(Keys.MERCHANT_ID, id)
            putString(Keys.MERCHANT_ACQUIRER, acquirer)
            apply()
        }
        println("Merchant info saved: $name, $city, $postalCode, $acquirer")
    }

    fun getMerchantName(context: Context): String {
        return getPrefs(context).getString(
            Keys.MERCHANT_NAME,
            Constants.DefaultMerchant.NAME
        ) ?: Constants.DefaultMerchant.NAME
    }

    fun getMerchantCity(context: Context): String {
        return getPrefs(context).getString(
            Keys.MERCHANT_CITY,
            Constants.DefaultMerchant.CITY
        ) ?: Constants.DefaultMerchant.CITY
    }

    fun getMerchantCategory(context: Context): String? {
        val category = getPrefs(context).getString(Keys.MERCHANT_CATEGORY, null)
        return if (category.isNullOrEmpty()) null else category
    }

    fun getMerchantPostalCode(context: Context): String? {
        return getPrefs(context).getString(Keys.POSTAL_CODE, null)
    }

    fun getMerchantId(context: Context): String? {
        return getPrefs(context).getString(Keys.MERCHANT_ID, null)
    }

    fun getMerchantAcquirer(context: Context): String? {
        return getPrefs(context).getString(Keys.MERCHANT_ACQUIRER, null)
    }

    fun getPostalCode(context: Context): String? {
        return getPrefs(context).getString(Keys.POSTAL_CODE, null)
    }

    fun saveFeeSettings(context: Context, feeType: String, feeValue: Double) {
        getPrefs(context).edit().apply {
            putString(Keys.FEE_TYPE, feeType)
            putString(Keys.FEE_VALUE, feeValue.toString())
            apply()
        }
        println("Fee settings saved: $feeType = $feeValue")
    }

    fun getFeeType(context: Context): String {
        return getPrefs(context).getString(
            Keys.FEE_TYPE,
            Constants.FeeType.NONE
        ) ?: Constants.FeeType.NONE
    }

    fun getFeeValue(context: Context): Double {
        val feeString = getPrefs(context).getString(Keys.FEE_VALUE, "0") ?: "0"
        return feeString.toDoubleOrNull() ?: 0.0
    }

    fun saveUserLogin(
        context: Context,
        userId: String,
        userName: String,
        userEmail: String
    ) {
        getPrefs(context).edit().apply {
            putString(Keys.USER_ID, userId)
            putString(Keys.USER_NAME, userName)
            putString(Keys.USER_EMAIL, userEmail)
            putBoolean(Keys.IS_LOGGED_IN, true)
            apply()
        }
        println("User logged in: $userName")
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(Keys.IS_LOGGED_IN, false)
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(Keys.USER_ID, null)
    }

    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(Keys.USER_NAME, null)
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(Keys.USER_EMAIL, null)
    }

    fun logout(context: Context) {
        getPrefs(context).edit().apply {
            remove(Keys.USER_ID)
            remove(Keys.USER_NAME)
            remove(Keys.USER_EMAIL)
            putBoolean(Keys.IS_LOGGED_IN, false)
            apply()
        }
        println("User logged out")
    }

    fun isFirstTime(context: Context): Boolean {
        return getPrefs(context).getBoolean(Keys.FIRST_TIME, true)
    }

    fun setNotFirstTime(context: Context) {
        getPrefs(context).edit().putBoolean(Keys.FIRST_TIME, false).apply()
    }

    fun saveThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(Keys.THEME_MODE, mode).apply()
    }

    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(Keys.THEME_MODE, "auto") ?: "auto"
    }

    fun saveLanguage(context: Context, language: String) {
        getPrefs(context).edit().putString(Keys.LANGUAGE, language).apply()
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(Keys.LANGUAGE, "id") ?: "id"
    }

    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
        println("All preferences cleared")
    }

    fun clearQRISSettings(context: Context) {
        getPrefs(context).edit().apply {
            remove(Keys.QRIS_PAYLOAD)
            remove(Keys.MERCHANT_NAME)
            remove(Keys.MERCHANT_CITY)
            remove(Keys.MERCHANT_CATEGORY)
            remove(Keys.MERCHANT_ID)
            remove(Keys.MERCHANT_ACQUIRER)
            remove(Keys.FEE_TYPE)
            remove(Keys.FEE_VALUE)
            apply()
        }
        println("QRIS settings cleared")
    }

    fun printAll(context: Context) {
        val prefs = getPrefs(context).all
        println("All Preferences:")
        prefs.forEach { (key, value) ->
            println("   $key = $value")
        }
    }
}