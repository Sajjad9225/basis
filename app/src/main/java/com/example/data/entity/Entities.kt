package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val username: String,
    val email: String,
    val passwordHash: String, // Plain hashed for simulation
    val country: String,
    val isVerified: Boolean = false,
    val verificationCode: String = "123456", // Default simulated pin
    val balance: Double = 0.0, // Promo balance $0
    val registrationDate: Long = System.currentTimeMillis(),
    val isSuspended: Boolean = false,
    val demoLevel: Int = 1,
    val winsCount: Int = 0,
    val lossesCount: Int = 0,
    val dailyPnlPercent: Double = 0.0,
    val customNotificationCount: Int = 0,
    val promoApplied: Boolean = false
) : Serializable

@Entity(tableName = "trade_orders")
data class TradeOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val symbol: String,
    val marketType: String, // CRYPTO, FOREX, INDEX, COMMODITY
    val side: String, // "BUY" or "SELL"
    val orderType: String, // "MARKET", "LIMIT", "STOP"
    val entryPrice: Double,
    val targetPrice: Double, // Price where it executes if limit/stop, or current execution price
    val units: Double,
    val leverage: Int = 10,
    val slPrice: Double? = null,
    val tpPrice: Double? = null,
    val status: String = "OPEN", // "OPEN", "PROCESSED_LIMIT", "CLOSED", "CANCELLED"
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val pnl: Double = 0.0
) : Serializable

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "TRANSFER_FUNDING", "TRANSFER_TRADING"
    val currency: String, // "USD", "BTC", "ETH", "USDT"
    val amount: Double,
    val fee: Double = 0.0,
    val paymentMethod: String, // "METAMASK", "CREDIT_CARD", "BANK_WIRE", "BTC_WALLET"
    val addressOrDetails: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED" // "PENDING", "COMPLETED", "REJECTED"
) : Serializable

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = false
) : Serializable
