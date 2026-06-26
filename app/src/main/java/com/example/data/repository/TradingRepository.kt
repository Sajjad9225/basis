package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

data class MarketItem(
    val symbol: String,
    val name: String,
    val type: String, // "crypto", "forex", "indices", "commodities"
    val basePrice: Double,
    val currentPrice: Double,
    val changePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: Double,
    val sparkline: List<Float> // Historic visual trend
)

class TradingRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val orderDao = db.tradeOrderDao()
    private val transactionDao = db.walletTransactionDao()
    private val announcementDao = db.announcementDao()

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Initial Static Markets list
    private var marketsList = listOf(
        // Crypto
        MarketItem("BTC/USDT", "Bitcoin", "crypto", 68250.0, 68250.0, 1.45, 69100.0, 67120.0, 28450123.0, generateInitialSparkline(68250.0)),
        MarketItem("ETH/USDT", "Ethereum", "crypto", 3540.0, 3540.0, -0.82, 3620.0, 3490.0, 14200500.0, generateInitialSparkline(3540.0)),
        MarketItem("SOL/USDT", "Solana", "crypto", 148.50, 148.50, 4.12, 153.20, 142.0, 6500400.0, generateInitialSparkline(148.50)),
        MarketItem("XRP/USDT", "Ripple", "crypto", 0.5250, 0.5250, 0.15, 0.5350, 0.5120, 1800200.0, generateInitialSparkline(0.5250)),
        
        // Forex
        MarketItem("EUR/USD", "Euro / US Dollar", "forex", 1.08520, 1.08520, 0.12, 1.08810, 1.08310, 89450000.0, generateInitialSparkline(1.08520)),
        MarketItem("GBP/USD", "Pound / US Dollar", "forex", 1.27250, 1.27250, -0.05, 1.27600, 1.26950, 65120000.0, generateInitialSparkline(1.27250)),
        MarketItem("USD/JPY", "US Dollar / Yen", "forex", 157.32, 157.32, 0.45, 157.85, 156.90, 11200000.0, generateInitialSparkline(157.32)),
        MarketItem("AUD/USD", "Aussie / US Dollar", "forex", 0.66420, 0.66420, -0.22, 0.66800, 0.66100, 42100000.0, generateInitialSparkline(0.66420)),

        // Indices
        MarketItem("NASDAQ", "NASDAQ 100", "indices", 19685.0, 19685.0, 1.15, 19742.0, 19520.0, 48201200.0, generateInitialSparkline(19685.0)),
        MarketItem("S&P500", "S&P 500 Index", "indices", 5431.50, 5431.50, 0.78, 5452.0, 5410.0, 35400000.0, generateInitialSparkline(5431.50)),
        MarketItem("DOW JONES", "Dow Jones 30", "indices", 39125.0, 39125.0, 0.42, 39310.0, 38920.0, 24500000.0, generateInitialSparkline(39125.0)),

        // Commodities
        MarketItem("GOLD", "Gold Spot", "commodities", 2322.80, 2322.80, -0.65, 2345.0, 2312.0, 1285000.0, generateInitialSparkline(2322.80)),
        MarketItem("SILVER", "Silver Spot", "commodities", 29.15, 29.15, -1.24, 29.70, 28.95, 450000.0, generateInitialSparkline(29.15)),
        MarketItem("CRUDE_OIL", "Brent Crude Oil", "commodities", 78.42, 78.42, 2.18, 79.20, 77.10, 895000.0, generateInitialSparkline(78.42))
    )

    private val _markets = MutableStateFlow(marketsList)
    val markets: StateFlow<List<MarketItem>> = _markets.asStateFlow()

    // Active session handling
    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    val currentUser: Flow<User?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else userDao.getAllUsers().map { list -> list.find { it.id == id } }
    }

    // Real-time online prices fetched from public APIs
    private var realBtcPrice: Double? = null
    private var realEthPrice: Double? = null
    private var realSolPrice: Double? = null
    private var realXrpPrice: Double? = null

    private var realEurPrice: Double? = null
    private var realGbpPrice: Double? = null
    private var realJpyPrice: Double? = null
    private var realAudPrice: Double? = null

    init {
        startOnlinePriceFetching()
        startPriceSimulation()
    }

    private fun fetchJsonFromUrl(urlString: String): String? {
        return try {
            val url = java.net.URL(urlString)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("TradingRepository", "Network fetch failed for $urlString: ${e.message}")
            null
        }
    }

    private fun fetchCryptoPrices() {
        val json = fetchJsonFromUrl("https://api.binance.com/api/v3/ticker/price") ?: return
        try {
            val array = org.json.JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val symbol = obj.getString("symbol")
                val price = obj.getDouble("price")
                when (symbol) {
                    "BTCUSDT" -> realBtcPrice = price
                    "ETHUSDT" -> realEthPrice = price
                    "SOLUSDT" -> realSolPrice = price
                    "XRPUSDT" -> realXrpPrice = price
                }
            }
        } catch (e: Exception) {
            Log.e("TradingRepository", "Error parsing crypto prices: ${e.message}")
        }
    }

    private fun fetchForexRates() {
        val json = fetchJsonFromUrl("https://api.frankfurter.app/latest?from=USD") ?: return
        try {
            val obj = org.json.JSONObject(json)
            val rates = obj.getJSONObject("rates")
            val eur = rates.getDouble("EUR")
            val gbp = rates.getDouble("GBP")
            val jpy = rates.getDouble("JPY")
            val aud = rates.getDouble("AUD")
            
            realEurPrice = 1.0 / eur
            realGbpPrice = 1.0 / gbp
            realJpyPrice = jpy
            realAudPrice = 1.0 / aud
        } catch (e: Exception) {
            Log.e("TradingRepository", "Error parsing forex rates: ${e.message}")
        }
    }

    private fun startOnlinePriceFetching() {
        repositoryScope.launch(Dispatchers.IO) {
            while (isActive) {
                fetchCryptoPrices()
                fetchForexRates()
                delay(6000) // update online prices every 6 seconds
            }
        }
    }

    private fun startPriceSimulation() {
        repositoryScope.launch {
            while (isActive) {
                delay(1500) // Price updates every 1.5s
                marketsList = marketsList.map { item ->
                    val variance = when (item.type) {
                        "crypto" -> Random.nextDouble(-0.002, 0.0022) // High volatility
                        "forex" -> Random.nextDouble(-0.0003, 0.00033) // Low volatility e.g. EURUSD
                        "indices" -> Random.nextDouble(-0.0008, 0.001)
                        else -> Random.nextDouble(-0.0012, 0.0014) // Commodities
                    }

                    val targetPrice = when (item.symbol) {
                        "BTC/USDT" -> realBtcPrice
                        "ETH/USDT" -> realEthPrice
                        "SOL/USDT" -> realSolPrice
                        "XRP/USDT" -> realXrpPrice
                        "EUR/USD" -> realEurPrice
                        "GBP/USD" -> realGbpPrice
                        "USD/JPY" -> realJpyPrice
                        "AUD/USD" -> realAudPrice
                        else -> null
                    }

                    val newPrice = if (targetPrice != null) {
                        // Apply micro-variations for live-feel sub-ticks
                        val subTickJitter = Random.nextDouble(-0.0004, 0.0004)
                        targetPrice * (1.0 + subTickJitter)
                    } else {
                        item.currentPrice * (1.0 + variance)
                    }

                    val changePct = ((newPrice - item.basePrice) / item.basePrice) * 100.0

                    // Keep lists capped to 15 historic visual points
                    val newSpark = item.sparkline.toMutableList()
                    newSpark.removeAt(0)
                    newSpark.add(newPrice.toFloat())

                    item.copy(
                        currentPrice = newPrice,
                        changePercent = changePct,
                        high24h = if (newPrice > item.high24h) newPrice else item.high24h,
                        low24h = if (newPrice < item.low24h) newPrice else item.low24h,
                        sparkline = newSpark
                    )
                }
                _markets.emit(marketsList)
                updateActiveTradePnl()
            }
        }
    }

    // Auto-update unrealized P&L for open positions based on live prices
    private suspend fun updateActiveTradePnl() {
        val activeId = _currentUserId.value ?: return
        val openOrders = firstOrNullFlow(orderDao.getActiveOrdersForUser(activeId)) ?: return
        
        for (order in openOrders) {
            val market = marketsList.find { it.symbol == order.symbol } ?: continue
            val currentPrice = market.currentPrice
            val multiplier = if (order.side == "BUY") 1.0 else -1.0
            
            // P&L = (CurrentPrice - EntryPrice) * Units * Leverage * Buyer/Seller direction multiplier
            val rawPnl = (currentPrice - order.entryPrice) * order.units * order.leverage * multiplier
            
            // Check SL/TP thresholds
            var closed = false
            var finalStatus = "OPEN"
            var executionPrice = currentPrice
            var finalPnl = rawPnl

            if (order.tpPrice != null) {
                if (order.side == "BUY" && currentPrice >= order.tpPrice) {
                    closed = true
                    executionPrice = order.tpPrice
                } else if (order.side == "SELL" && currentPrice <= order.tpPrice) {
                    closed = true
                    executionPrice = order.tpPrice
                }
            }

            if (!closed && order.slPrice != null) {
                if (order.side == "BUY" && currentPrice <= order.slPrice) {
                    closed = true
                    executionPrice = order.slPrice
                } else if (order.side == "SELL" && currentPrice >= order.slPrice) {
                    closed = true
                    executionPrice = order.slPrice
                }
            }

            if (closed) {
                finalPnl = (executionPrice - order.entryPrice) * order.units * order.leverage * multiplier
                val updatedOrder = order.copy(
                    status = "CLOSED",
                    closeTime = System.currentTimeMillis(),
                    pnl = finalPnl
                )
                orderDao.updateOrder(updatedOrder)
                adjustUserBalanceAndStats(order.userId, finalPnl)
            } else {
                orderDao.updateOrder(order.copy(pnl = rawPnl))
            }
        }
    }

    private suspend fun adjustUserBalanceAndStats(userId: Int, pnl: Double) {
        val user = userDao.getUserById(userId) ?: return
        val isWin = pnl > 0
        userDao.updateUser(user.copy(
            balance = user.balance + pnl,
            winsCount = if (isWin) user.winsCount + 1 else user.winsCount,
            lossesCount = if (!isWin) user.lossesCount + 1 else user.lossesCount
        ))
    }

    private fun generateInitialSparkline(price: Double): List<Float> {
        val list = mutableListOf<Float>()
        var current = price
        for (i in 0 until 15) {
            current = current * (1.0 + Random.nextDouble(-0.01, 0.01))
            list.add(current.toFloat())
        }
        return list
    }

    private suspend fun <T> firstOrNullFlow(flow: Flow<T>): T? {
        return flow.take(1).singleOrNull()
    }

    // Auth actions
    fun setCurrentUserId(id: Int?) {
        _currentUserId.value = id
    }

    suspend fun registerUser(fullName: String, username: String, email: String, passwordHash: String, country: String): User? {
        // Validation check
        val existing = userDao.getUserByUsernameOrEmail(username) ?: userDao.getUserByUsernameOrEmail(email)
        if (existing != null) return null

        val randomVerifiedCode = (100000..999999).random().toString()
        val defaultUser = User(
            fullName = fullName,
            username = username,
            email = email,
            passwordHash = passwordHash,
            country = country,
            isVerified = false,
            verificationCode = randomVerifiedCode,
            balance = 0.0, // Demo balance 0
            registrationDate = System.currentTimeMillis()
        )
        val newId = userDao.insertUser(defaultUser)
        return defaultUser.copy(id = newId.toInt())
    }

    suspend fun verifyEmail(email: String, code: String): Boolean {
        val user = userDao.getUserByEmail(email) ?: return false
        if (user.verificationCode == code) {
            userDao.updateUser(user.copy(isVerified = true))
            return true
        }
        return false
    }

    suspend fun loginUser(identity: String, passwordHash: String): User? {
        val user = userDao.getUserByUsernameOrEmail(identity) ?: return null
        if (user.passwordHash == passwordHash) {
            if (user.isSuspended) {
                // Return null or specify suspended user in downstream state
                return null
            }
            _currentUserId.value = user.id
            return user
        }
        return null
    }

    suspend fun logoutUser() {
        _currentUserId.value = null
    }

    // User modifications (Profile and admin operations)
    suspend fun updateUserProfile(user: User) {
        userDao.updateUser(user)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun applyPromoCode(userId: Int): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (!user.promoApplied) {
            // Apply $200 promo bonus or level scale
            userDao.updateUser(user.copy(
                balance = user.balance + 200.0,
                promoApplied = true
            ))
            return true
        }
        return false
    }

    // Trading interface functions
    suspend fun placeOrder(
        userId: Int,
        symbol: String,
        marketType: String,
        side: String,
        orderType: String,
        units: Double,
        entryPrice: Double,
        leverage: Int,
        slPrice: Double?,
        tpPrice: Double?
    ): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (user.isSuspended) return false

        // In virtual trading, checking if demo account has sufficient leverage margin
        val marginRequired = (entryPrice * units) / leverage
        if (user.balance < marginRequired) {
            return false // Insufficient funds
        }

        val order = TradeOrder(
            userId = userId,
            symbol = symbol,
            marketType = marketType,
            side = side,
            orderType = orderType,
            entryPrice = entryPrice,
            targetPrice = entryPrice,
            units = units,
            leverage = leverage,
            slPrice = slPrice,
            tpPrice = tpPrice,
            status = "OPEN"
        )
        orderDao.insertOrder(order)
        return true
    }

    suspend fun closeOrder(orderId: Int) {
        val order = orderDao.getOrderById(orderId) ?: return
        if (order.status != "OPEN") return

        val market = marketsList.find { it.symbol == order.symbol } ?: return
        val currentPrice = market.currentPrice
        val multiplier = if (order.side == "BUY") 1.0 else -1.0
        val finalPnl = (currentPrice - order.entryPrice) * order.units * order.leverage * multiplier

        val closedOrder = order.copy(
            status = "CLOSED",
            closeTime = System.currentTimeMillis(),
            pnl = finalPnl
        )
        orderDao.updateOrder(closedOrder)
        adjustUserBalanceAndStats(order.userId, finalPnl)
    }

    suspend fun cancelOrder(orderId: Int) {
        val order = orderDao.getOrderById(orderId) ?: return
        if (order.status == "OPEN") {
            orderDao.updateOrder(order.copy(status = "CANCELLED", closeTime = System.currentTimeMillis()))
        }
    }

    // Wallets & Transactions
    suspend fun executeDeposit(userId: Int, amount: Double, method: String, address: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        
        // Save transaction
        val tx = WalletTransaction(
            userId = userId,
            type = "DEPOSIT",
            currency = "USD",
            amount = amount,
            paymentMethod = method,
            addressOrDetails = address,
            status = "COMPLETED"
        )
        transactionDao.insertTransaction(tx)
        
        // Update user balance (simulated deposit adds to demo wallet)
        userDao.updateUser(user.copy(balance = user.balance + amount))
        return true
    }

    suspend fun executePendingDeposit(userId: Int, amount: Double, method: String, address: String): Boolean {
        val tx = WalletTransaction(
            userId = userId,
            type = "DEPOSIT",
            currency = "USDT",
            amount = amount,
            paymentMethod = method,
            addressOrDetails = address,
            status = "PENDING"
        )
        transactionDao.insertTransaction(tx)
        return true
    }

    suspend fun executeWithdrawal(userId: Int, amount: Double, method: String, address: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (user.balance < amount) return false

        val tx = WalletTransaction(
            userId = userId,
            type = "WITHDRAWAL",
            currency = "USD",
            amount = amount,
            paymentMethod = method,
            addressOrDetails = address,
            status = "COMPLETED"
        )
        transactionDao.insertTransaction(tx)

        userDao.updateUser(user.copy(balance = user.balance - amount))
        return true
    }

    suspend fun executeTransfer(userId: Int, amount: Double, fromWallet: String, toWallet: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (user.balance < amount) return false

        val tx = WalletTransaction(
            userId = userId,
            type = "TRANSFER_FUNDING",
            currency = "USD",
            amount = amount,
            paymentMethod = "INTERNAL_TRANSFER",
            addressOrDetails = "From $fromWallet to $toWallet",
            status = "COMPLETED"
        )
        transactionDao.insertTransaction(tx)
        return true
    }

    // Flows for UI Components
    fun getTransactions(userId: Int): Flow<List<WalletTransaction>> = transactionDao.getTransactionsForUser(userId)
    fun getActivePositions(userId: Int): Flow<List<TradeOrder>> = orderDao.getActiveOrdersForUser(userId)
    fun getTradeHistory(userId: Int): Flow<List<TradeOrder>> = orderDao.getAllOrdersForUser(userId)

    // Admin flows & methods
    fun adminGetAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    fun adminGetAllTransactions(): Flow<List<WalletTransaction>> = transactionDao.getAllTransactions()
    fun adminGetAllOrders(): Flow<List<TradeOrder>> = orderDao.getAllOrders()

    suspend fun adminUpdateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun adminResetAccount(userId: Int) {
        val user = userDao.getUserById(userId) ?: return
        // Delete all actions and reset balance back to standard bonus
        userDao.updateUser(user.copy(
            balance = 0.0,
            winsCount = 0,
            lossesCount = 0,
            isSuspended = false,
            demoLevel = 1
        ))
    }

    // Announcements
    fun getAnnouncements(): Flow<List<Announcement>> = announcementDao.getAllAnnouncements()
    suspend fun addAnnouncement(title: String, msg: String, isUrgent: Boolean) {
        announcementDao.insertAnnouncement(Announcement(title = title, message = msg, isUrgent = isUrgent))
    }
    suspend fun deleteAnnouncement(id: Int) {
        announcementDao.deleteAnnouncement(id)
    }
}
