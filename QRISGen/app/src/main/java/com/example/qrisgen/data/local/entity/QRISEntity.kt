package com.example.qrisgen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.qrisgen.data.model.MerchantInfo
import com.example.qrisgen.utils.Constants

@Entity(
    tableName = Constants.TABLE_NAME,
    indices = [Index(value = ["timestamp"])]
)
data class QRISEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "original_payload")
    val originalPayload: String,

    @ColumnInfo(name = "generated_payload")
    val generatedPayload: String? = null,

    val amount: Long,
    val feeType: String = Constants.FeeType.NONE,
    val feeValue: Double = 0.0,

    @Embedded(prefix = "merchant_")
    val merchant: MerchantInfo,

    val timestamp: Long = System.currentTimeMillis()
)
