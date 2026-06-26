package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE username = :query OR email = :query LIMIT 1")
    suspend fun getUserByUsernameOrEmail(query: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}

@Dao
interface TradeOrderDao {
    @Query("SELECT * FROM trade_orders WHERE userId = :userId ORDER BY openTime DESC")
    fun getAllOrdersForUser(userId: Int): Flow<List<TradeOrder>>

    @Query("SELECT * FROM trade_orders WHERE userId = :userId AND status = 'OPEN' ORDER BY openTime DESC")
    fun getActiveOrdersForUser(userId: Int): Flow<List<TradeOrder>>

    @Query("SELECT * FROM trade_orders ORDER BY openTime DESC")
    fun getAllOrders(): Flow<List<TradeOrder>>

    @Query("SELECT * FROM trade_orders WHERE id = :id")
    suspend fun getOrderById(id: Int): TradeOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TradeOrder): Long

    @Update
    suspend fun updateOrder(order: TradeOrder)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: Int): Flow<List<WalletTransaction>>

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: WalletTransaction)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Int)
}
