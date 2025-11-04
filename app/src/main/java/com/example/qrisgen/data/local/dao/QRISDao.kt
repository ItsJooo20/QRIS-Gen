package com.example.qrisgen.data.local.dao

import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Dao
import androidx.room.Insert
import com.example.qrisgen.data.local.entity.QRISEntity
import com.example.qrisgen.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface QRISDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: QRISEntity): Long

    @Query("SELECT * FROM ${Constants.TABLE_NAME} ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<QRISEntity>>

    @Query("SELECT * FROM ${Constants.TABLE_NAME} WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): QRISEntity?

    @Delete
    suspend fun deleteTransaction(transaction: QRISEntity): Int

    @Query("DELETE FROM ${Constants.TABLE_NAME} WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Long): Int

    @Query("DELETE FROM ${Constants.TABLE_NAME}")
    suspend fun deleteAllTransactions()
}