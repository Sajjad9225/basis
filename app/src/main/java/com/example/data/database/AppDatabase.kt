package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.*
import com.example.data.entity.*

@Database(
    entities = [
        User::class,
        TradeOrder::class,
        WalletTransaction::class,
        Announcement::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tradeOrderDao(): TradeOrderDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apextrade_sim_database"
                )
                .fallbackToDestructiveMigration() // Automatic migration since it is a simulator
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
