package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.*
import com.example.data.repository.MarketItem
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TradingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TradingRepository(application)

    // Reactive streams from Repository
    val markets: StateFlow<List<MarketItem>> = repository.markets
    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUserId: StateFlow<Int?> = repository.currentUserId

    // Live queries that listen to changes
    val activePositions: StateFlow<List<TradeOrder>> = currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getActivePositions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tradeHistory: StateFlow<List<TradeOrder>> = currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getTradeHistory(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions: StateFlow<List<WalletTransaction>> = currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = repository.getAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin-specific flows
    val adminAllUsers: StateFlow<List<User>> = repository.adminGetAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllTransactions: StateFlow<List<WalletTransaction>> = repository.adminGetAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllOrders: StateFlow<List<TradeOrder>> = repository.adminGetAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen navigation state
    var currentScreenState by mutableStateOf("LANDING") // "LANDING", "LOGIN", "REGISTER", "VERIFY", "DASHBOARD", "MARKETS", "TERMINAL", "WALLET", "DEPOSIT", "WITHDRAW", "TRANSACTIONS", "PROFILE", "NOTIFICATIONS", "SECURITY", "SUPPORT", "ADMIN_LOGIN", "ADMIN_DASHBOARD"
    
    // Remember Me login persistence state
    var rememberMe by mutableStateOf(true)

    init {
        val sharedPrefs = application.getSharedPreferences("exchange_prefs", Context.MODE_PRIVATE)
        val savedUserId = sharedPrefs.getInt("remembered_user_id", -1)
        if (savedUserId != -1) {
            viewModelScope.launch {
                val user = repository.getUserById(savedUserId)
                if (user != null && !user.isSuspended) {
                    repository.setCurrentUserId(user.id)
                    setSymbol("BTC/USDT")
                    currentScreenState = "DASHBOARD"
                }
            }
        }
    }
    
    // Auth inputs
    var loginInput by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    var regFullName by mutableStateOf("")
    var regUsername by mutableStateOf("")
    var regEmail by mutableStateOf("")
    var regPassword by mutableStateOf("")
    var regConfirmPassword by mutableStateOf("")
    var regCountry by mutableStateOf("")
    var regTermsAgreed by mutableStateOf(false)
    var emailVerifyInput by mutableStateOf("")
    
    // Active simulation trading states
    var selectedSymbol by mutableStateOf("BTC/USDT")
    var orderSide by mutableStateOf("BUY") // "BUY" or "SELL"
    var orderType by mutableStateOf("MARKET") // "MARKET", "LIMIT", "STOP"
    var tradeAmount by mutableStateOf("0.5")  // units
    var limitPriceInput by mutableStateOf("")
    var slPriceInput by mutableStateOf("")
    var tpPriceInput by mutableStateOf("")
    var leverageFactor by mutableStateOf(10) // slider factor

    // Wallet Inputs
    var depositAmountInput by mutableStateOf("500")
    var depositSelectedMethod by mutableStateOf("METAMASK") // "METAMASK", "CREDIT_CARD", "BTC_WALLET"
    var walletAddressState by mutableStateOf("0x89205A20C1C4dce84cF44CFE1eA4bCFB2A99E8F3")
    
    var withdrawalAmountInput by mutableStateOf("")
    var withdrawalAddressInput by mutableStateOf("")
    var transferAmountInput by mutableStateOf("")
    var transferFromInput by mutableStateOf("Trading Account")
    var transferToInput by mutableStateOf("Funding Account")

    var promoCodeInput by mutableStateOf("")
    var showWithdrawalFeeDialog by mutableStateOf(false)

    // Modals & Alert states
    var showPremiumModal by mutableStateOf(false)
    var showSuccessDialog by mutableStateOf(false)
    var successDialogMessage by mutableStateOf("")
    var errorAlertMessage by mutableStateOf<String?>(null)

    // Admin states
    var adminSearchQuery by mutableStateOf("")
    var selectedAdminUser by mutableStateOf<User?>(null)
    var customBonusInput by mutableStateOf("")
    var customAnnTitle by mutableStateOf("")
    var customAnnMsg by mutableStateOf("")

    // Verification emails list helper
    var emailForVerification by mutableStateOf("")

    fun setSymbol(symbol: String) {
        selectedSymbol = symbol
        val market = markets.value.find { it.symbol == symbol }
        if (market != null) {
            limitPriceInput = String.format("%.2f", market.currentPrice)
            slPriceInput = String.format("%.2f", market.currentPrice * 0.95)
            tpPriceInput = String.format("%.2f", market.currentPrice * 1.1)
        }
    }

    // AUTH PROCESSORS
    fun handleRegister() {
        if (regFullName.isBlank() || regUsername.isBlank() || regEmail.isBlank() || regPassword.isBlank() || regCountry.isBlank()) {
            errorAlertMessage = "All registration fields must be complete."
            return
        }
        if (regPassword != regConfirmPassword) {
            errorAlertMessage = "Passwords do not match."
            return
        }
        if (!regTermsAgreed) {
            errorAlertMessage = "You must agree to the terms and regulations."
            return
        }

        viewModelScope.launch {
            val user = repository.registerUser(
                fullName = regFullName,
                username = regUsername,
                email = regEmail,
                passwordHash = regPassword, // Simulated hashing
                country = regCountry
            )
            if (user != null) {
                emailForVerification = regEmail
                currentScreenState = "VERIFY"
                successDialogMessage = "Registration successful! A simulated 6-digit code has been dispatched to: $regEmail. Default simulation code is '${user.verificationCode}'."
                showSuccessDialog = true
            } else {
                errorAlertMessage = "Registration failed. Username or Email already in use!"
            }
        }
    }

    fun handleVerify() {
        if (emailVerifyInput.isBlank()) {
            errorAlertMessage = "Verification code is required."
            return
        }
        viewModelScope.launch {
            val success = repository.verifyEmail(emailForVerification, emailVerifyInput)
            if (success) {
                successDialogMessage = "Account verified and Virtual Trading activated with $9,500 bonus! Please login to begin simulation."
                showSuccessDialog = true
                currentScreenState = "LOGIN"
                loginInput = emailForVerification
            } else {
                errorAlertMessage = "Invalid verification code! Check the code in the confirmation dialog."
            }
        }
    }

    fun handleLogin() {
        if (loginInput.isBlank() || loginPassword.isBlank()) {
            errorAlertMessage = "Please fill in all details."
            return
        }
        viewModelScope.launch {
            val user = repository.loginUser(loginInput, loginPassword)
            if (user != null) {
                if (user.isSuspended) {
                    errorAlertMessage = "This simulated account has been suspended by the platform administrator."
                    return@launch
                }
                
                // Store remember me state
                val sharedPrefs = getApplication<Application>().getSharedPreferences("exchange_prefs", Context.MODE_PRIVATE)
                if (rememberMe) {
                    sharedPrefs.edit().putInt("remembered_user_id", user.id).apply()
                } else {
                    sharedPrefs.edit().remove("remembered_user_id").apply()
                }

                successDialogMessage = "Welcome Back, ${user.fullName}! Entering Simulated Trading Workspace."
                showSuccessDialog = true
                // Pre-configure initial default selected symbol
                setSymbol("BTC/USDT")
                currentScreenState = "DASHBOARD"
            } else {
                errorAlertMessage = "Login failed. Invalid username/email or password."
            }
        }
    }

    fun handleLogout() {
        viewModelScope.launch {
            // Clear remember me preference on formal logout
            val sharedPrefs = getApplication<Application>().getSharedPreferences("exchange_prefs", Context.MODE_PRIVATE)
            sharedPrefs.edit().remove("remembered_user_id").apply()

            repository.logoutUser()
            loginInput = ""
            loginPassword = ""
            currentScreenState = "LANDING"
        }
    }

    fun triggerPremiumModal() {
        showPremiumModal = true
    }

    // SIMULATION TRADING OPERATORS
    fun handlePlaceOrder() {
        val user = currentUser.value ?: return
        val unitsValue = tradeAmount.toDoubleOrNull() ?: 0.0
        if (unitsValue <= 0.0) {
            errorAlertMessage = "Please input a valid positive trade unit value."
            return
        }

        val mkt = markets.value.find { it.symbol == selectedSymbol } ?: return
        val entryPrice = if (orderType == "LIMIT") {
            limitPriceInput.toDoubleOrNull() ?: mkt.currentPrice
        } else {
            mkt.currentPrice
        }

        viewModelScope.launch {
            val success = repository.placeOrder(
                userId = user.id,
                symbol = selectedSymbol,
                marketType = mkt.type,
                side = orderSide,
                orderType = orderType,
                units = unitsValue,
                entryPrice = entryPrice,
                leverage = leverageFactor,
                slPrice = slPriceInput.toDoubleOrNull(),
                tpPrice = tpPriceInput.toDoubleOrNull()
            )

            if (success) {
                successDialogMessage = "Position successfully entered! $orderSide ${tradeAmount} units of $selectedSymbol with ${leverageFactor}x leverage."
                showSuccessDialog = true
            } else {
                errorAlertMessage = "Insufficient virtual margin! Leverage factor ${leverageFactor}x cannot cover this contract size. Consider reducing volume or adding virtual deposit funds."
            }
        }
    }

    fun handleCloseOrder(orderId: Int) {
        viewModelScope.launch {
            repository.closeOrder(orderId)
        }
    }

    // WALLET SIMULATOR OPERATORS
    fun handleDeposit() {
        val user = currentUser.value ?: return
        val amount = depositAmountInput.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            errorAlertMessage = "Invalid deposit sum specified."
            return
        }

        viewModelScope.launch {
            val success = repository.executeDeposit(
                userId = user.id,
                amount = amount,
                method = depositSelectedMethod,
                address = walletAddressState
            )
            if (success) {
                successDialogMessage = "Successfully credited $amount USD bonus funds onto your Trading Account."
                showSuccessDialog = true
                currentScreenState = "WALLET"
            }
        }
    }

    fun handleApplyPromoCode(userId: Int) {
        val code = promoCodeInput.trim().uppercase()
        if (code.isEmpty()) {
            errorAlertMessage = "Please enter a valid promo code first."
            return
        }
        val bonus = when (code) {
            "ARC3244" -> 6500.0
            "ARC3344" -> 8500.0
            "ARC1122" -> 1000.0
            else -> null
        }
        if (bonus == null) {
            errorAlertMessage = "The entered promo code is invalid. Please check the spelling and try again."
            return
        }

        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                // Execute a deposit transaction to Room to increment balance and record history
                val success = repository.executeDeposit(user.id, bonus, "PROMO_CODE", "System Gift: $code")
                if (success) {
                    successDialogMessage = "Congratulations! Promo code $code has been activated successfully, adding $bonus USD to your virtual account."
                    showSuccessDialog = true
                    promoCodeInput = ""
                } else {
                    errorAlertMessage = "Registration failed or promo code is already applied."
                }
            } else {
                errorAlertMessage = "Error loading user profile statistics."
            }
        }
    }

    fun handleConfirmWithdrawalFeeDeposit() {
        val amount = depositAmountInput.toDoubleOrNull() ?: 20.0
        val address = "0xf280a2e9dcC4A160ac5dAC1E5030eC2eC411A45E"
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                repository.executePendingDeposit(user.id, amount, "USDT_BEP20", address)
                successDialogMessage = "Your transfer fee deposit of $amount USDT has been registered successfully and is now in Pending Review. Verification has started and will be completed shortly."
                showSuccessDialog = true
                currentScreenState = "WALLET"
            } else {
                errorAlertMessage = "Error loading user account information."
            }
        }
    }

    fun executeUserWithdrawal(onCompleted: () -> Unit) {
        val amountStr = withdrawalAmountInput.trim()
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            errorAlertMessage = "Please enter a valid withdrawal request amount greater than zero."
            return
        }

        val userVal = currentUser.value
        if (userVal == null) {
            errorAlertMessage = "Error loading user statistics. Please try again."
            return
        }

        val address = withdrawalAddressInput.trim()
        val bep20Regex = Regex("^0x[a-fA-F0-9]{40}$")
        if (!bep20Regex.matches(address)) {
            errorAlertMessage = "Invalid address! Destination wallet address must belong to the BEP-20 network (must start with 0x followed by exactly 40 hexadecimal characters, total 42 characters)."
            return
        }

        // Intercept with the specific $20 USD network fee warning instruction popup
        showWithdrawalFeeDialog = true
    }

    fun handleWithdrawal() {
        // ALWAYS trigger premium fee warning modal for withdrawals/premium overrides as requested
        triggerPremiumModal()
    }

    fun handleTransfer() {
        // Transfer triggers simulation limits
        triggerPremiumModal()
    }

    // ADMIN OVERRIDES
    fun handleAdminLogin(pass: String) {
        if (pass == "Admin@2026") {
            currentScreenState = "ADMIN_DASHBOARD"
            loginPassword = ""
        } else {
            errorAlertMessage = "Invalid administrative passcode."
        }
    }

    fun handleAdminSuspendToggle(user: User) {
        viewModelScope.launch {
            repository.adminUpdateUser(user.copy(isSuspended = !user.isSuspended))
            // Refresh selection
            selectedAdminUser?.let {
                if (it.id == user.id) {
                    selectedAdminUser = user.copy(isSuspended = !user.isSuspended)
                }
            }
        }
    }

    fun handleAdminVerificationToggle(user: User) {
        viewModelScope.launch {
            repository.adminUpdateUser(user.copy(isVerified = !user.isVerified))
            selectedAdminUser?.let {
                if (it.id == user.id) {
                    selectedAdminUser = user.copy(isVerified = !user.isVerified)
                }
            }
        }
    }

    fun handleAdminAdjustBonus(userId: Int, isIncrease: Boolean) {
        val delta = customBonusInput.toDoubleOrNull() ?: 0.0
        if (delta <= 0.0) {
            errorAlertMessage = "Must input a positive numeric adjustment sum."
            return
        }
        viewModelScope.launch {
            val user = adminAllUsers.value.find { it.id == userId } ?: return@launch
            val newBal = if (isIncrease) user.balance + delta else user.balance - delta
            repository.adminUpdateUser(user.copy(balance = newBal))
            customBonusInput = ""
            selectedAdminUser = user.copy(balance = newBal)
            successDialogMessage = "Simulated Balance adjusted by ${if (isIncrease) "+" else "-"}$delta USD successfully."
            showSuccessDialog = true
        }
    }

    fun handleAdminReset(userId: Int) {
        viewModelScope.launch {
            repository.adminResetAccount(userId)
            val updated = adminAllUsers.value.find { it.id == userId }
            if (updated != null) {
                selectedAdminUser = updated
            }
            successDialogMessage = "User Virtual Account has been fully restored to factory simulator parameters ($9,500 simulator default balance)."
            showSuccessDialog = true
        }
    }

    fun handleAdminAddAnnouncement() {
        if (customAnnTitle.isBlank() || customAnnMsg.isBlank()) {
            errorAlertMessage = "Please input the notification heading and text body."
            return
        }
        viewModelScope.launch {
            repository.addAnnouncement(customAnnTitle, customAnnMsg, isUrgent = true)
            customAnnTitle = ""
            customAnnMsg = ""
            successDialogMessage = "Strategic Broadcast announcement sent successfully and visible on user dashboards."
            showSuccessDialog = true
        }
    }

    fun handleAdminDeleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
        }
    }
}
