package com.example.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReceiptDao {
    @Insert
    suspend fun insert(receipt: Receipt)

    @Query("SELECT SUM(amount) FROM receipts")
    suspend fun getTotalAmount(): Double?

    @Query("DELETE FROM receipts")
    suspend fun clearAll()
}
