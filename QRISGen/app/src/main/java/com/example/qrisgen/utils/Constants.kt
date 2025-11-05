package com.example.qrisgen.utils

object Constants {

    // 1. Database constants
    const val DATABASE_NAME = "qris_app.db"
    const val DATABASE_VERSION = 1
    const val TABLE_NAME = "transactions"

    // 2. EMV Tags
    object EMVTags {
        const val PAYLOAD_FORMAT = "00"
        const val POINT_OF_INITIATION = "01"
        const val MERCHANT_ACCOUNT = "26"
        const val MERCHANT_CATEGORY = "52"
        const val TRANSACTION_CURRENCY = "53"
        const val TRANSACTION_AMOUNT = "54"
        const val TIP_INDICATOR = "55"
        const val TIP_FIXED = "56"
        const val TIP_PERCENTAGE = "57"
        const val COUNTRY_CODE = "58"
        const val MERCHANT_NAME = "59"
        const val MERCHANT_CITY = "60"
        const val POSTAL_CODE = "61"
        const val ADDITIONAL_DATA = "62"
        const val CRC = "63"

        // Sub-tags for Merchant Account (26)
        object MerchantAccount {
            const val GLOBAL_UNIQUE_ID = "00"
            const val MERCHANT_PAN = "01"
        }
    }

    // 3. Default merchant info
    object DefaultMerchant {
        const val NAME = "TEST MERCHANT"
        const val CITY = "JAKARTA"
        const val COUNTRY = "ID"
        const val CURRENCY_CODE = "360"
    }

    // 4. SharedPreferences keys
    const val PREF_NAME = "merchant_prefs"
    const val KEY_MERCHANT_NAME = "merchant_name"
    const val KEY_MERCHANT_CITY = "merchant_city"
    const val KEY_MERCHANT_COUNTRY = "merchant_country"
    const val KEY_QRIS_PAYLOAD = "qris_payload"
    const val KEY_FEE_TYPE = "fee_type"
    const val KEY_FEE_VALUE = "fee_value"
    const val KEY_MERCHANT_CATEGORY = "merchant_category"
    const val KEY_POSTAL_CODE = "postal_code"
    const val KEY_CURRENCY_CODE = "currency_code"

    // 5. Other constants
    const val QR_CODE_SIZE = 512
    const val CURRENCY_IDR = "360"

    // 6. Point of Initiation Values
    object InitiationMethod {
        const val STATIC = "11"
        const val DYNAMIC = "12"
    }

    // 7. Fee
    object FeeType {
        const val NONE = "none"
        const val FIXED = "fixed"
        const val PERCENTAGE = "percentage"
    }
}
