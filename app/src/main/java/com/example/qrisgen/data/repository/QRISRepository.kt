package com.example.qrisgen.data.repository

import com.example.qrisgen.data.local.dao.QRISDao
import com.example.qrisgen.data.local.entity.QRISEntity
import com.example.qrisgen.data.model.MerchantInfo
import com.example.qrisgen.data.model.ParsedQRIS
import com.example.qrisgen.data.model.QRISTransaction
import com.example.qrisgen.utils.QRISGenerator
import com.example.qrisgen.utils.QRISParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QRISRepository(private val qrisDao: QRISDao) {

    val allTransactions: Flow<List<QRISTransaction>> = qrisDao.getAllTransactions()
        .map { entities ->
            entities.map { it.toTransaction() }
        }

    fun parseQRIS(payload: String): ParsedQRIS {
        return QRISParser.parseQRIS(payload)
    }

    fun validateQRIS(payload: String): Boolean {
        return QRISParser.isValidQRIS(payload)
    }

    fun generateDynamicQRIS(
        originalPayload: String,
        amount: Long,
        feeType: String,
        feeValue: Double,
        customMerchantName: String? = null,
        customMerchantCity: String? = null,
        customMerchantPostCode: String? = null,
    ): String {
        return QRISGenerator.generateDynamicQRIS(
            staticPayload = originalPayload,
            amount = amount,
            feeType = feeType,
            feeValue = feeValue,
            customMerchantName = customMerchantName,
            customMerchantCity = customMerchantCity,
            customMerchantPostCode = customMerchantPostCode,
        )
    }

    suspend fun saveTransaction(transaction: QRISTransaction): Long {
        val entity = transaction.toEntity()
        return qrisDao.insertTransaction(entity)
    }

    suspend fun getTransactionById(id: Long): QRISTransaction? {
        return qrisDao.getTransactionById(id)?.toTransaction()
    }

    suspend fun deleteTransaction(transaction: QRISTransaction): Int {
        val entity = transaction.toEntity()
        return qrisDao.deleteTransaction(entity)
    }

    suspend fun deleteTransactionById(id: Long): Int {
        return qrisDao.deleteTransactionById(id)
    }

    suspend fun deleteAllTransactions() {
        qrisDao.deleteAllTransactions()
    }
}

fun QRISEntity.toTransaction(): QRISTransaction {
    return QRISTransaction(
        id = this.id,
        originalPayload = this.originalPayload,
        generatedPayload = this.generatedPayload,
        amount = this.amount,
        feeType = this.feeType,
        feeValue = this.feeValue,
        merchant = this.merchant,
        timestamp = this.timestamp
    )
}

fun QRISTransaction.toEntity(): QRISEntity {
    return QRISEntity(
        id = this.id,
        originalPayload = this.originalPayload,
        generatedPayload = this.generatedPayload,
        amount = this.amount,
        feeType = this.feeType,
        feeValue = this.feeValue,
        merchant = this.merchant,
        timestamp = this.timestamp
    )
}