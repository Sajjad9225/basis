package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.TradingViewModel
import java.text.SimpleDateFormat
import java.util.*

// Helper to format timestamps gracefully
fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}

// Global Core Layout Container - Left navigation and core layout grids
@Composable
fun AppNavigatorLayout(
    viewModel: TradingViewModel,
    content: @Composable () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val rawUserId by viewModel.currentUserId.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        containerColor = BackgroundDark,
        bottomBar = {
            // Display secure navigation controls if user has active trading session
            if (rawUserId != null && viewModel.currentScreenState != "LANDING" && viewModel.currentScreenState != "LOGIN" && viewModel.currentScreenState != "REGISTER" && viewModel.currentScreenState != "VERIFY") {
                NavigationBar(
                    containerColor = CardBackground,
                    tonalElevation = 8.dp,
                    modifier = Modifier.border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    val screens = listOf(
                        Triple("DASHBOARD", "Home", Icons.Filled.Dashboard),
                        Triple("TERMINAL", "Terminal", Icons.Filled.CandlestickChart),
                        Triple("MARKETS", "Markets", Icons.Filled.TrendingUp),
                        Triple("WALLET", "Wallet", Icons.Filled.AccountBalanceWallet),
                        Triple("PROFILE", "Account", Icons.Filled.Person)
                    )
                    screens.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = viewModel.currentScreenState == route || (route == "WALLET" && (viewModel.currentScreenState == "DEPOSIT" || viewModel.currentScreenState == "WITHDRAW" || viewModel.currentScreenState == "TRANSACTIONS")),
                            onClick = { viewModel.currentScreenState = route },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryAccent,
                                selectedTextColor = PrimaryAccent,
                                indicatorColor = Color(0x33FF6B00),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
            
            // Standard Alerts & Premium Mode Prompt Modals
            if (viewModel.showPremiumModal) {
                PremiumWarningModal(onDismiss = { viewModel.showPremiumModal = false })
            }

            if (viewModel.showWithdrawalFeeDialog) {
                WithdrawalFeeInstructionDialog(
                    onDismiss = { viewModel.showWithdrawalFeeDialog = false },
                    onNavigateToDeposit = {
                        viewModel.showWithdrawalFeeDialog = false
                        viewModel.depositSelectedMethod = "USDT_BEP20"
                        viewModel.depositAmountInput = "20"
                        viewModel.currentScreenState = "DEPOSIT"
                    },
                    walletAddress = "0xf280a2e9dcC4A160ac5dAC1E5030eC2eC411A45E"
                )
            }

            if (viewModel.showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.showSuccessDialog = false },
                    confirmButton = {
                        TextButton(onClick = { viewModel.showSuccessDialog = false }) {
                            Text("Confirm", color = PrimaryAccent)
                        }
                    },
                    containerColor = CardBackground,
                    title = { Text("ApexTrade Core Dialog", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text(viewModel.successDialogMessage, color = TextSecondary) }
                )
            }

            viewModel.errorAlertMessage?.let { msg ->
                AlertDialog(
                    onDismissRequest = { viewModel.errorAlertMessage = null },
                    confirmButton = {
                        TextButton(onClick = { viewModel.errorAlertMessage = null }) {
                            Text("Acknowledge", color = PrimaryAccent)
                        }
                    },
                    containerColor = CardBackground,
                    title = { Text("Simulator Notice", color = DangerRed, fontWeight = FontWeight.Bold) },
                    text = { Text(msg, color = TextSecondary) }
                )
            }
        }
    }
}

// 1. Landing Hero Page
@Composable
fun LandingScreen(viewModel: TradingViewModel) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F0600), BackgroundDark)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Brand Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PrimaryAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.OfflineBolt,
                    contentDescription = "Logo",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "APEXTRADE",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Large display Typography
        Text(
            "Next-Generation Liquidity Terminal",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "The world's premier crypto currency & forex simulated exchange dashboard. Built for elite speed with zero real monetary risks.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Virtual bonus card badges
        GlassCard(
            cornerRadius = 24.dp,
            borderGlow = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0x18FF6B00), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = "Promo",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Real-Time Simulated Terminal Included",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Activate instant trading terminal with absolute simulated portfolios and full admin metrics.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Navigation Action Buttons Custom Styled
        Button(
            onClick = { viewModel.currentScreenState = "REGISTER" },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("get_started_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LAUNCH TERMINAL CORE", fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { viewModel.currentScreenState = "LOGIN" },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text("ENTER TRADING DESK", fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hidden / Secret link to Admin Hub (Simulated hidden entry)
        Row(
            modifier = Modifier
                .clickable { viewModel.currentScreenState = "ADMIN_LOGIN" }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Security, contentDescription = "Admin", tint = Color(0x22FFFFFF), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("System Diagnostics Entrance", color = Color(0x22FFFFFF), fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "*Disclaimer: ApexTrade is an educational simulation framework. All funds, deposits, withdrawal activities, positions, and analytics remain virtual. No real cash is exchanged or accessible.*",
            color = Color(0x66FFFFFF),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// 2. User Registration Forms
@Composable
fun RegisterScreen(viewModel: TradingViewModel) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Safe navigation arrow
        IconButton(
            onClick = { viewModel.currentScreenState = "LANDING" },
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text(
            "Create Terminal Trading Account",
            fontSize = 26.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Register below to activate your virtual trading simulator terminal in real-time.",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = viewModel.regFullName,
            onValueChange = { viewModel.regFullName = it },
            label = { Text("Full Name", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_fullname")
        )

        OutlinedTextField(
            value = viewModel.regUsername,
            onValueChange = { viewModel.regUsername = it },
            label = { Text("Username", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("reg_username")
        )

        OutlinedTextField(
            value = viewModel.regEmail,
            onValueChange = { viewModel.regEmail = it },
            label = { Text("Email Address", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = viewModel.regPassword,
            onValueChange = { viewModel.regPassword = it },
            label = { Text("Password", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = viewModel.regConfirmPassword,
            onValueChange = { viewModel.regConfirmPassword = it },
            label = { Text("Confirm Password", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = viewModel.regCountry,
            onValueChange = { viewModel.regCountry = it },
            label = { Text("Country", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable { viewModel.regTermsAgreed = !viewModel.regTermsAgreed }
        ) {
            Checkbox(
                checked = viewModel.regTermsAgreed,
                onCheckedChange = { viewModel.regTermsAgreed = it },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "I agree to simulate responsibly and respect standard virtual regulations.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Button(
            onClick = { viewModel.handleRegister() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("register_btn")
        ) {
            Text("REGISTER & START VERIFICATION", fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already registered? ", color = TextSecondary, fontSize = 13.sp)
            Text(
                "Login",
                color = PrimaryAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { viewModel.currentScreenState = "LOGIN" }
            )
        }
    }
}

// 3. User Login Page
@Composable
fun LoginScreen(viewModel: TradingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(
            onClick = { viewModel.currentScreenState = "LANDING" },
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text(
            "Access Liquidity Desk",
            fontSize = 26.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Log in to enter your luxury trading workstation terminal screen.",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = viewModel.loginInput,
            onValueChange = { viewModel.loginInput = it },
            label = { Text("Username or Email Address", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("login_username")
        )

        OutlinedTextField(
            value = viewModel.loginPassword,
            onValueChange = { viewModel.loginPassword = it },
            label = { Text("Security Password", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("login_password")
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.rememberMe,
                    onCheckedChange = { viewModel.rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Remember session", color = TextSecondary, fontSize = 12.sp)
            }
            Text(
                "Simulated Password Reset?",
                color = SecondaryAccent,
                fontSize = 12.sp,
                modifier = Modifier.clickable {
                    viewModel.errorAlertMessage = "To reset passwords in simulated environment, contact the administrator database directly using admin panel, or register a new identity."
                }
            )
        }

        Button(
            onClick = { viewModel.handleLogin() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("login_btn")
        ) {
            Text("ENTER TRADING DASHBOARD", fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("No simulation portfolio? ", color = TextSecondary, fontSize = 13.sp)
            Text(
                "Register here",
                color = PrimaryAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { viewModel.currentScreenState = "REGISTER" }
            )
        }
    }
}

// 4. Verification Screen
@Composable
fun EmailVerifyScreen(viewModel: TradingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0x11FF6B00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = "Verify", tint = PrimaryAccent, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Account Activation Code",
            fontSize = 22.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Enter the 6-digit simulation PIN mapped to ${viewModel.emailForVerification}",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp, start = 12.dp, end = 12.dp)
        )

        OutlinedTextField(
            value = viewModel.emailVerifyInput,
            onValueChange = { viewModel.emailVerifyInput = it },
            label = { Text("6-Digit Verification PIN", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center),
            modifier = Modifier
                .width(220.dp)
                .padding(bottom = 32.dp)
        )

        Button(
            onClick = { viewModel.handleVerify() },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("ACTIVATE WORKSPACE", fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Did not receive simulated PIN? Click below to dispatch another simulator trigger.",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable {
                    viewModel.errorAlertMessage = "Simulated notification re-dispatched. Primary authentication overrides code remains the default verified database entry shown in the preceding dialog."
                }
                .padding(8.dp)
        )
    }
}

// 5. Core Dashboard Welcome Page (Interactive grid matching multi-sidebar request)
@Composable
fun LiveWithdrawalsTicker() {
    val items = remember {
        mutableStateListOf(
            Pair("0x7a8d...f901", "850.00"),
            Pair("0x4fd2...7b82", "1,250.00"),
            Pair("0x92f4...0a1d", "420.00"),
            Pair("0x1ab3...8e21", "2,980.00"),
            Pair("0x7e5c...46db", "150.00")
        )
    }

    LaunchedEffect(Unit) {
        val prefixes = listOf("0x3a", "0x5c", "0x9d", "0x12", "0x6f", "0x7b", "0xe3", "0xa4", "0xab", "0xcd")
        val suffixes = listOf("f82d", "d11a", "cc21", "9e40", "28ba", "190f", "e734", "5bf2", "aa81", "7efc")
        while (true) {
            kotlinx.coroutines.delay(3500)
            val randomAddress = "${prefixes.random()}...${suffixes.random()}"
            val roundedAmount = (150..6500).random() + (0..99).random() / 100.0
            val formattedAmount = String.format("%,.2f", roundedAmount)
            items.add(0, Pair(randomAddress, formattedAmount))
            if (items.size > 5) {
                items.removeAt(items.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0C0C0C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0x1925D366), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF25D366), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Live Success Withdrawal Monitor (BEP-20 Network)",
                color = Color(0xFF25D366),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items.forEach { (addr, amt) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Address $addr",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "Withdrawn $amt USD",
                    color = SuccessGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: TradingViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val markets by viewModel.markets.collectAsState()
    val activeOrders by viewModel.activePositions.collectAsState()
    val txs by viewModel.userTransactions.collectAsState()

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val isMultiPane = screenWidth > 600

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        item {
            // High Fidelity welcome Card Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(PrimaryAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user?.username?.take(2)?.uppercase() ?: "AP",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Welcome Back, ${user?.fullName ?: "Trader"}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Terminal ID: #${user?.id ?: 0 + 1000}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (user?.isVerified == true) Color(0x2200D26A) else Color(0x22FF4747),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (user?.isVerified == true) "VERIFIED" else "UNVERIFIED",
                                    color = if (user?.isVerified == true) SuccessGreen else DangerRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Promo bonus claim
                IconButton(onClick = {
                    viewModel.handleApplyPromoCode(user?.id ?: return@IconButton)
                }) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = "Promo Action", tint = PrimaryAccent)
                }
            }
        }

        // Live Portfolios balance and Left Sidebar metric overview
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VIRTUAL PORTFOLIO SUMMARY", color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                        Text("LEVEL ${user?.demoLevel ?: 1}", color = PrimaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = String.format("$%,.2f", user?.balance ?: 0.0),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Win Ratio", color = TextSecondary, fontSize = 11.sp)
                            val wins = user?.winsCount ?: 0
                            val losses = user?.lossesCount ?: 0
                            val winRate = if (wins + losses == 0) 75.0 else (wins.toDouble() / (wins + losses)) * 100.0
                            Text(String.format("%.1f %%", winRate), color = SuccessGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Active Open Trades", color = TextSecondary, fontSize = 11.sp)
                            Text("${activeOrders.size} Positions", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Horizontal scrolling selector of Watchlist Markets (Center elements)
        item {
            Text("WATCHLIST SIMULATOR", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 18.dp)
            ) {
                val topCrypto = markets.take(4)
                topCrypto.forEach { market ->
                    val changeColor = if (market.changePercent >= 0) SuccessGreen else DangerRed
                    val changeSign = if (market.changePercent >= 0) "+" else ""
                    Card(
                        modifier = Modifier
                            .width(135.dp)
                            .padding(end = 10.dp)
                            .clickable {
                                viewModel.setSymbol(market.symbol)
                                viewModel.currentScreenState = "TERMINAL"
                            },
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, Color(0x0EFFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(market.symbol, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("$%,.2f", market.currentPrice), color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = String.format("%s%.2f%%", changeSign, market.changePercent),
                                color = changeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Trade Positions table below
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("OPEN SIMULATOR POSITIONS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Launch Terminal",
                    color = PrimaryAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.currentScreenState = "TERMINAL" }
                )
            }
        }

        if (activeOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Analytics, contentDescription = "None", tint = Color(0x22FFFFFF), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active simulator contracts open.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(activeOrders) { order ->
                val mkt = markets.find { it.symbol == order.symbol }
                val currentPrice = mkt?.currentPrice ?: order.entryPrice
                val isBuy = order.side == "BUY"
                val scale = if (isBuy) 1.0 else -1.0
                val calculatedPnl = (currentPrice - order.entryPrice) * order.units * order.leverage * scale
                val isPnlPositive = calculatedPnl >= 0

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    padding = 12.dp,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isBuy) Color(0x3300D26A) else Color(0x33FF4747),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        order.side,
                                        color = if (isBuy) SuccessGreen else DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(order.symbol, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${order.leverage}x", color = SecondaryAccent, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Entry: $%,.4f".format(order.entryPrice), color = TextSecondary, fontSize = 11.sp)
                            Text("Current: $%,.4f".format(currentPrice), color = TextSecondary, fontSize = 11.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format("%s$%,.2f", if (isPnlPositive) "+" else "", calculatedPnl),
                                color = if (isPnlPositive) SuccessGreen else DangerRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { viewModel.handleCloseOrder(order.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("CLOSE", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }


    }
}

// 6. Markets Listing Dashboard
@Composable
fun MarketsScreen(viewModel: TradingViewModel) {
    val markets by viewModel.markets.collectAsState()
    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "crypto", "forex", "indices", "commodities"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Text("MARKET FEED", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Real-time cryptocurrency, forex indices, and global spot commodities feed.", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Categories Pickers horizontal selectors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            val categories = listOf("ALL", "crypto", "forex", "indices", "commodities")
            categories.forEach { cat ->
                val isActive = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(
                            if (isActive) PrimaryAccent else CardBackground,
                            RoundedCornerShape(10.dp)
                        )
                        .border(1.dp, if (isActive) Color.Transparent else Color(0x11FFFFFF), RoundedCornerShape(10.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        cat.uppercase(),
                        color = if (isActive) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val filteredMarkets = markets.filter { selectedCategory == "ALL" || it.type == selectedCategory }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredMarkets) { market ->
                val signPrice = if (market.changePercent >= 0) "+" else ""
                val trendColor = if (market.changePercent >= 0) SuccessGreen else DangerRed

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable {
                            viewModel.setSymbol(market.symbol)
                            viewModel.currentScreenState = "TERMINAL"
                        },
                    padding = 12.dp,
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0x0CFFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    market.symbol.take(2),
                                    color = PrimaryAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(market.symbol, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(market.name, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(100.dp))
                            }
                        }

                        // Compact Sparkline graph inline
                        SparklineChart(prices = market.sparkline, lineColor = trendColor)

                        Column(horizontalAlignment = Alignment.End) {
                            Text(String.format("$%,.4f", market.currentPrice), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%s%.2f%%", signPrice, market.changePercent),
                                color = trendColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 7. Core Interactive Trading Terminal
@Composable
fun TerminalScreen(viewModel: TradingViewModel) {
    val markets by viewModel.markets.collectAsState()
    val activeOrders by viewModel.activePositions.collectAsState()
    val currSymbol = viewModel.selectedSymbol
    val marketData = markets.find { it.symbol == currSymbol } ?: markets.first()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Upper ticker header metrics panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(currSymbol, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.width(8.dp))
                val typeLabel = marketData.type.uppercase()
                Box(
                    modifier = Modifier
                        .background(Color(0x33FF6B00), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(typeLabel, color = PrimaryAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format("$%,.4f", marketData.currentPrice), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                val signPercent = if (marketData.changePercent >= 0) "+" else ""
                Text(String.format("%s%.2f%%", signPercent, marketData.changePercent), color = if (marketData.changePercent >= 0) SuccessGreen else DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center section: advanced interactive canvas candles drawing
        MainCandlestickChart(symbol = currSymbol, currentPrice = marketData.currentPrice)

        Spacer(modifier = Modifier.height(16.dp))

        // Right Sidebar trading parameters controller configuration
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 16.dp
        ) {
            Column {
                // BUY SELL Switch tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Button(
                        onClick = { viewModel.orderSide = "BUY" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.orderSide == "BUY") SuccessGreen else Color(0x12FFFFFF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("BUY / LONG", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { viewModel.orderSide = "SELL" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.orderSide == "SELL") DangerRed else Color(0x12FFFFFF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SELL / SHORT", fontWeight = FontWeight.Bold)
                    }
                }

                // Limit orders toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("MARKET", "LIMIT", "STOP").forEach { type ->
                        val isSelected = viewModel.orderType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                                .background(
                                    if (isSelected) PrimaryAccent else Color(0x0CFFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.orderType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(type, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Volume fields units inputs
                OutlinedTextField(
                    value = viewModel.tradeAmount,
                    onValueChange = { viewModel.tradeAmount = it },
                    label = { Text("Simulated Contract Units", color = TextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedIndicatorColor = PrimaryAccent
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("terminal_units")
                )

                // Limit prices triggers if limit/stop selected
                if (viewModel.orderType == "LIMIT" || viewModel.orderType == "STOP") {
                    OutlinedTextField(
                        value = viewModel.limitPriceInput,
                        onValueChange = { viewModel.limitPriceInput = it },
                        label = { Text("Trigger Rate Limit Price ($)", color = TextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryAccent
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )
                }

                // Stop loss and take profits fields
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = viewModel.slPriceInput,
                        onValueChange = { viewModel.slPriceInput = it },
                        label = { Text("Stop Loss (SL)", color = TextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryAccent
                        ),
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.tpPriceInput,
                        onValueChange = { viewModel.tpPriceInput = it },
                        label = { Text("Take Profit (TP)", color = TextSecondary) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryAccent
                        ),
                        modifier = Modifier.weight(1f).padding(start = 6.dp)
                    )
                }

                // Leverage sliding ratios
                Text(
                    "Selected Contract Leverage: ${viewModel.leverageFactor}x",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = viewModel.leverageFactor.toFloat(),
                    onValueChange = { viewModel.leverageFactor = it.toInt() },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryAccent,
                        activeTrackColor = PrimaryAccent
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = { viewModel.handlePlaceOrder() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.orderSide == "BUY") SuccessGreen else DangerRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("execute_order_btn")
                ) {
                    Text("EXECUTE SIMULATED ORDER", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 8. Wallets Dashboard Overview Page
@Composable
fun WalletScreen(viewModel: TradingViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        item {
            Text("EXCHANGE WALLETS", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Virtual exchange wallets overview.", color = TextSecondary, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Balance Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("VIRTUAL TRADING BALANCES", color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("$%,.2f", user?.balance ?: 0.0),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("USD", fontSize = 14.sp, color = PrimaryAccent, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.currentScreenState = "DEPOSIT" },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Deposit")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DEPOSIT", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.executeUserWithdrawal { /* noop */ } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Withdraw")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WITHDRAW", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.handleTransfer() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x0CFFFFFF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "Transfer")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TRANSFER", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Promo Code Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "REDEEM PROMO CODE",
                        color = PrimaryAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Enter a valid promo code below to top up your virtual simulation balance directly.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.promoCodeInput,
                            onValueChange = { viewModel.promoCodeInput = it },
                            placeholder = { Text("e.g. ARC3244", color = Color(0x44FFFFFF), fontSize = 11.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0x11FFFFFF),
                                unfocusedContainerColor = Color(0x0AFFFFFF),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedIndicatorColor = PrimaryAccent,
                                unfocusedIndicatorColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.weight(1f).height(48.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.handleApplyPromoCode(user?.id ?: return@Button) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("APPLY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BEP-20 Instant Withdrawal Form Card (Relocated Here!)
            Text("WITHDRAWAL PORTAL", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Instant BEP-20 Withdrawal Portal",
                            color = PrimaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0x1125D366), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "BSC - BEP20",
                                color = Color(0xFF25D366),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Please enter your BEP-20 wallet address. An invalid address might result in transaction failure or loss on the simulated blockchain.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.withdrawalAddressInput,
                        onValueChange = { viewModel.withdrawalAddressInput = it },
                        label = { Text("BEP-20 Wallet Address (Starts with 0x)", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("0x...", color = Color(0x33FFFFFF), fontSize = 11.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.withdrawalAmountInput,
                        onValueChange = { viewModel.withdrawalAmountInput = it },
                        label = { Text("Withdrawal Amount ($)", color = TextSecondary, fontSize = 11.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = PrimaryAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.executeUserWithdrawal {
                                // successful withdrawal complete callback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text("Confirm & Fast Transfer to Address", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Continuous rolling window for various withdrawals
                    LiveWithdrawalsTicker()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("RECENT TRANSACTION LOGS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    "History",
                    color = PrimaryAccent,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { viewModel.currentScreenState = "TRANSACTIONS" }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent fund transactions recorded.", color = TextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            items(transactions) { tx ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    cornerRadius = 12.dp,
                    padding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tx.type, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (tx.status == "PENDING") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFA000), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("PENDING", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text(formatTimestamp(tx.timestamp), color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val isPending = tx.status == "PENDING"
                            val color = if (isPending) Color(0xFFFFA000) else if (tx.type == "DEPOSIT") SuccessGreen else DangerRed
                            val sign = if (tx.type == "DEPOSIT") "+" else "-"
                            Text(
                                text = String.format("%s$%,.2f", sign, tx.amount),
                                color = color,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(if (isPending) "USDT (REVIEW)" else "USD (SIMULATED)", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// 9. Wallet Deposit Details Screen
@Composable
fun DepositScreen(viewModel: TradingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        IconButton(onClick = { viewModel.currentScreenState = "WALLET" }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("Simulated Funding Terminal", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Provide mock funding settings to add virtual USD onto your simulated balance.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            listOf("METAMASK", "CREDIT_CARD", "BTC_WALLET", "USDT_BEP20").forEach { m ->
                val isSelected = viewModel.depositSelectedMethod == m
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .background(if (isSelected) PrimaryAccent else Color(0x11FFFFFF), RoundedCornerShape(8.dp))
                        .clickable { viewModel.depositSelectedMethod = m }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (m == "USDT_BEP20") "USDT BEP20" else m.replace("_", " "),
                        color = if (isSelected) Color.Black else TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (viewModel.depositSelectedMethod == "USDT_BEP20") {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            var isCopied by remember { mutableStateOf(false) }
            val schoolWallet = "0xf280a2e9dcC4A160ac5dAC1E5030eC2eC411A45E"

            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                cornerRadius = 16.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "USDT BEP-20 Network Fee Deposit",
                        color = PrimaryAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Please deposit the 20 USDT network transfer fee to our official BEP-20 wallet address below. Click confirm once sent to queue your withdrawal as Pending review.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_usdt_qr_1781960281708),
                        contentDescription = "USDT BEP20 Official QR Code",
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(schoolWallet) })
                                isCopied = true
                            }
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = schoolWallet,
                                color = PrimaryAccent,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isCopied) "Address Copied!" else "Click to copy official address",
                                color = if (isCopied) SuccessGreen else TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = viewModel.depositAmountInput,
                onValueChange = { viewModel.depositAmountInput = it },
                label = { Text("Submitted Network Fee Amount (USD)", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = PrimaryAccent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            Button(
                onClick = { viewModel.handleConfirmWithdrawalFeeDeposit() },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Confirm Network Fee Payment", color = Color.Black, fontWeight = FontWeight.Bold)
            }

        } else {
            OutlinedTextField(
                value = viewModel.depositAmountInput,
                onValueChange = { viewModel.depositAmountInput = it },
                label = { Text("Simulation Credit Sum ($)", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = PrimaryAccent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).testTag("dep_amount")
            )

            OutlinedTextField(
                value = viewModel.walletAddressState,
                onValueChange = { viewModel.walletAddressState = it },
                label = { Text("Simulated Funding Address Override", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = PrimaryAccent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )

            Button(
                onClick = { viewModel.handleDeposit() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_deposit_btn")
            ) {
                Text("CLAIM MOCK FUND ALLOCATIONS", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 10. Withdraw Page Warning Indicator Wrapper
@Composable
fun PremiumWarningModal(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Text("Acknowledge", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF141414),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = "Locked", tint = PrimaryAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Virtual Account Limitation", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "Your account is currently operating in Standard Virtual Mode.\n\nTo activate advanced platform access and unlock additional features, a one-time activation fee of $200 is required.\n\nThis payment does not represent an investment and does not provide access to real financial withdrawals.\n\nAll balances, profits, and trading activities remain simulated.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    )
}

@Composable
fun WithdrawalFeeInstructionDialog(
    onDismiss: () -> Unit,
    onNavigateToDeposit: () -> Unit,
    walletAddress: String
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToDeposit,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Go to Deposit", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close", fontSize = 11.sp)
                }
            }
        },
        containerColor = Color(0xFF141414),
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = "Locked", tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Insufficient Balance",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You do not have enough funds to complete this withdrawal directly. A 20 USD network transfer fee must be deposited to this official BEP-20 wallet address below first:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Display QR Code
                Image(
                    painter = painterResource(id = com.example.R.drawable.img_usdt_qr_1781960281708),
                    contentDescription = "USDT BEP20 QR Code",
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    "Payment Network: BNB Smart Chain (BEP-20)",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Address Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(8.dp))
                        .clickable {
                            clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(walletAddress) })
                            isCopied = true
                        }
                        .padding(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = walletAddress,
                            color = PrimaryAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCopied) "Wallet address copied!" else "Click here to copy wallet address",
                            color = if (isCopied) SuccessGreen else TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

// 11. Transaction list histories Screen
@Composable
fun TransactionsScreen(viewModel: TradingViewModel) {
    val txs by viewModel.userTransactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        IconButton(onClick = { viewModel.currentScreenState = "WALLET" }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("TRANSACTIONS GENERAL GLOSSARY", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        if (txs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past simulated deposits or actions logged.", color = TextSecondary)
            }
        } else {
            LazyColumn {
                items(txs) { tx ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (tx.status == "PENDING") "REVIEW " else "MOCK " + tx.type, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (tx.status == "PENDING") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFFFA000), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("PENDING", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(formatTimestamp(tx.timestamp), color = TextSecondary, fontSize = 11.sp)
                                Text("Transfer Point Override: " + tx.paymentMethod, color = TextSecondary, fontSize = 11.sp)
                            }
                            val isPending = tx.status == "PENDING"
                            val color = if (isPending) Color(0xFFFFA000) else if (tx.type == "DEPOSIT") SuccessGreen else DangerRed
                            Text(
                                text = String.format("$ %,.2f", tx.amount),
                                color = color,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 12. Account & Profile Screen
@Composable
fun ProfileScreen(viewModel: TradingViewModel) {
    val user by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("WORKSTATION ACCOUNT PROFILE", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Button(
                onClick = { viewModel.handleLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("LOGOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Profile card details
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .background(Color(0x33FF6B00), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.fullName?.take(2)?.uppercase() ?: "AP",
                        color = PrimaryAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(user?.fullName ?: "Apex Sim User", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("@" + (user?.username ?: "username"), color = TextSecondary, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Divider(color = Color(0x11FFFFFF))

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email Location", color = TextSecondary, fontSize = 13.sp)
                    Text(user?.email ?: "", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Country Origin", color = TextSecondary, fontSize = 13.sp)
                    Text(user?.country ?: "Global", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Registered Sync", color = TextSecondary, fontSize = 13.sp)
                    Text(formatTimestamp(user?.registrationDate ?: System.currentTimeMillis()), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Verification Mode", color = TextSecondary, fontSize = 13.sp)
                    Text(if (user?.isVerified == true) "PASSED SIM VALIDATION" else "PENDING ACTION", color = if (user?.isVerified == true) SuccessGreen else DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security, Alerts buttons options links
        listOf(
            Triple("SECURITY CENTER", "Configure dynamic two-factor settings & access levels", Icons.Filled.Lock),
            Triple("NOTIFICATIONS OVERRIDES", "Platform alerts and global broadcast announcements", Icons.Filled.Notifications),
            Triple("SUPPORT ASSISTANCE", "Interactive customer simulator chat & FAQ modules", Icons.Filled.SupportAgent)
        ).forEach { (title, sub, icon) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clickable {
                        when (title) {
                            "SECURITY CENTER" -> viewModel.currentScreenState = "SECURITY"
                            "NOTIFICATIONS OVERRIDES" -> viewModel.currentScreenState = "NOTIFICATIONS"
                            "SUPPORT ASSISTANCE" -> viewModel.currentScreenState = "SUPPORT"
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color(0x06FFFFFF))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x0CFFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = title, tint = PrimaryAccent)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(sub, color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 13. Security Center Screen
@Composable
fun SecurityScreen(viewModel: TradingViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        IconButton(onClick = { viewModel.currentScreenState = "PROFILE" }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("Security Hub", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Virtual protective override managers.", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 24.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulated Two Factor Security (2FA)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Add Google Authenticator verification rules.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAccent, checkedTrackColor = Color(0x66FF6B00)))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0x11FFFFFF))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Instant Biometrics Logs", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Enforce fingerprints unlock rules.", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(checked = false, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = PrimaryAccent))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                viewModel.successDialogMessage = "Simulated keychains and crypto profiles saved."
                viewModel.showSuccessDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("SAVE SECURITY PARAMS", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

// 14. Notifications and Broadcast Screens
@Composable
fun NotificationsScreen(viewModel: TradingViewModel) {
    val annState by viewModel.announcements.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        IconButton(onClick = { viewModel.currentScreenState = "PROFILE" }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("BROADCASTS & ALERTS", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        if (annState.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No strategic system alerts open currently.", color = TextSecondary)
            }
        } else {
            LazyColumn {
                items(annState) { item ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        cornerRadius = 16.dp,
                        borderGlow = item.isUrgent
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FlashOn, contentDescription = "Flash", tint = PrimaryAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(item.message, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(formatTimestamp(item.timestamp), color = Color(0x44FFFFFF), fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

// 15. Support Center Simulated Messaging
@Composable
fun SupportScreen(viewModel: TradingViewModel) {
    var userMessage by remember { mutableStateOf("") }
    val chatLogs = remember {
        mutableStateListOf(
            Pair("APEXTRADE RECP", "Greetings from the Simulator Liquidity Desk. How can our administrative agents assist you? Notice: All dialogs remain simulated.")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        IconButton(onClick = { viewModel.currentScreenState = "PROFILE" }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Text("CUSTOMER SERVICE DESK", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Simulated responsive AI help assistance chatbot.", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Support Channels
        Text("Direct Customer Helpline & Tickets", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current

            // Telegram Channels Button
            Button(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://t.me/Avaxacademyy")
                    )
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // fallback or notify
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088CC)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Telegram",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Telegram: @Avaxacademyy", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // WhatsApp Direct Channel Button
            Button(
                onClick = {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://wa.me/")
                    )
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(44.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Forum,
                    contentDescription = "WhatsApp",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp Helpdesk", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat lists box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0C0C0C), RoundedCornerShape(16.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chatLogs) { (sender, text) ->
                    val isUser = sender == "USER"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Text(sender, fontSize = 9.sp, color = if (isUser) PrimaryAccent else SecondaryAccent, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isUser) Color(0x1AFF6B00) else Color(0x0CFFFFFF),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(text, color = TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                label = { Text("Describe queries here...", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = PrimaryAccent
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userMessage.isNotBlank()) {
                        chatLogs.add(Pair("USER", userMessage))
                        val question = userMessage
                        userMessage = ""
                        // Autoreply rules
                        val response = when {
                            question.contains("withdraw", ignoreCase = true) -> "Simulated deposits and funds remain educational. Unlock codes or $200 fees apply directly to mock simulations limits."
                            question.contains("verify", ignoreCase = true) -> "Accounts can be verified instantly by checking verification codes or using admin login override permissions."
                            else -> "Our simulated fintech desk recorded your inquiry: '$question'. An executive bot will solve it in simulated turns."
                        }
                        chatLogs.add(Pair("APEXTRADE RECP", response))
                    }
                },
                modifier = Modifier
                    .background(PrimaryAccent, CircleShape)
                    .size(46.dp)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = TextPrimary)
            }
        }
    }
}

// 16. Secret Admin Passcode Entrada Gate
@Composable
fun AdminLoginScreen(viewModel: TradingViewModel) {
    var codeText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0x33FF6B00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.SettingsSystemDaydream, contentDescription = "Lock", tint = PrimaryAccent, modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Platform Diagnostics", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Provide administrative security PIN to oversee active simulation accounts.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = codeText,
            onValueChange = { codeText = it },
            label = { Text("Security Access Passcode", color = TextSecondary) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = PrimaryAccent
            ),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.width(260.dp).padding(bottom = 24.dp).testTag("admin_passcode")
        )

        Button(
            onClick = { viewModel.handleAdminLogin(codeText) },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(260.dp).height(48.dp).testTag("admin_login_btn")
        ) {
            Text("AUTHENTICATE ADMIN", fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Cancel entry",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.clickable { viewModel.currentScreenState = "LANDING" }
        )
    }
}

// 17. Master Hidden Admin Dashboard Control Center
@Composable
fun AdminDashboardScreen(viewModel: TradingViewModel) {
    val users by viewModel.adminAllUsers.collectAsState()
    val transactions by viewModel.adminAllTransactions.collectAsState()
    val activeOrders by viewModel.adminAllOrders.collectAsState()

    val filteredUsers = remember(users, viewModel.adminSearchQuery) {
        if (viewModel.adminSearchQuery.isBlank()) users
        else users.filter {
            it.fullName.contains(viewModel.adminSearchQuery, ignoreCase = true) ||
            it.username.contains(viewModel.adminSearchQuery, ignoreCase = true) ||
            it.email.contains(viewModel.adminSearchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).background(PrimaryAccent, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Terminal, contentDescription = "Diag", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("APEX CORE DIAGNOSTICS", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Text(
                "Exit Admin",
                color = DangerRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.currentScreenState = "LANDING" }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Stat Grid blocks inside Admin Panel
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            listOf(
                Pair("TOTAL TRADERS", "${users.size} Accounts"),
                Pair("SIMULATED TXS", "${transactions.size} Logs"),
                Pair("OPEN CONTRAX", "${activeOrders.size} Positions")
            ).forEach { (lbl, valStr) ->
                Card(
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(lbl, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text(valStr, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Send Global Platform Notification Announcements
        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), padding = 12.dp) {
            Column {
                Text("BROADCAST ANNOUNCEMENT SYSTEM", color = PrimaryAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.customAnnTitle,
                        onValueChange = { viewModel.customAnnTitle = it },
                        label = { Text("Title", fontSize = 11.sp, color = TextSecondary) },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark)
                    )
                    OutlinedTextField(
                        value = viewModel.customAnnMsg,
                        onValueChange = { viewModel.customAnnMsg = it },
                        label = { Text("Broadcast Body Message", fontSize = 11.sp, color = TextSecondary) },
                        modifier = Modifier.weight(2f).padding(start = 4.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { viewModel.handleAdminAddAnnouncement() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("DISPATCH BROADCAST TO Dashboards", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Interactive listing searching of users
        OutlinedTextField(
            value = viewModel.adminSearchQuery,
            onValueChange = { viewModel.adminSearchQuery = it },
            label = { Text("Search users by username, email or name...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            colors = TextFieldDefaults.colors(focusedContainerColor = CardBackground, unfocusedContainerColor = CardBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        Text("ACTIVE SYSTEM USERS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredUsers) { usr ->
                val isSel = viewModel.selectedAdminUser?.id == usr.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { viewModel.selectedAdminUser = if (isSel) null else usr },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) Color(0x33FF6B00) else CardBackground
                    ),
                    border = BorderStroke(1.dp, if (isSel) PrimaryAccent else Color(0x0EFFFFFF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(usr.fullName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("@${usr.username} [${usr.email}]", color = TextSecondary, fontSize = 11.sp)
                            }
                            Text(
                                text = String.format("$ %,.2f", usr.balance),
                                color = SuccessGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Detailed expanded admin profile controls
                        if (isSel) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0x1AFFFFFF))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = { viewModel.handleAdminSuspendToggle(usr) }) {
                                    Text(
                                        if (usr.isSuspended) "UNSUSPEND SIM" else "SUSPEND USER",
                                        color = DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { viewModel.handleAdminVerificationToggle(usr) }) {
                                    Text(
                                        if (usr.isVerified) "REVOKE VERIFIED" else "VERIFY INSTANTLY",
                                        color = SecondaryAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = { viewModel.handleAdminReset(usr.id) }) {
                                    Text("RESTORE FAC DEF", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Manual bonus credits increment and decrements inputs
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                OutlinedTextField(
                                    value = viewModel.customBonusInput,
                                    onValueChange = { viewModel.customBonusInput = it },
                                    label = { Text("Balance Adjustment Override", fontSize = 10.sp, color = TextSecondary) },
                                    modifier = Modifier.weight(1.5f).height(48.dp),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = BackgroundDark, unfocusedContainerColor = BackgroundDark)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { viewModel.handleAdminAdjustBonus(usr.id, isIncrease = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("ADD (+)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = { viewModel.handleAdminAdjustBonus(usr.id, isIncrease = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("SUB (-)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
