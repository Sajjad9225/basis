package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TradingViewModel
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: TradingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
        
        AppNavigatorLayout(viewModel = viewModel) {
          when (viewModel.currentScreenState) {
            "LANDING" -> LandingScreen(viewModel = viewModel)
            "REGISTER" -> RegisterScreen(viewModel = viewModel)
            "LOGIN" -> LoginScreen(viewModel = viewModel)
            "VERIFY" -> EmailVerifyScreen(viewModel = viewModel)
            "DASHBOARD" -> DashboardScreen(viewModel = viewModel)
            "MARKETS" -> MarketsScreen(viewModel = viewModel)
            "TERMINAL" -> TerminalScreen(viewModel = viewModel)
            "WALLET" -> WalletScreen(viewModel = viewModel)
            "DEPOSIT" -> DepositScreen(viewModel = viewModel)
            "TRANSACTIONS" -> TransactionsScreen(viewModel = viewModel)
            "PROFILE" -> ProfileScreen(viewModel = viewModel)
            "SECURITY" -> SecurityScreen(viewModel = viewModel)
            "NOTIFICATIONS" -> NotificationsScreen(viewModel = viewModel)
            "SUPPORT" -> SupportScreen(viewModel = viewModel)
            "ADMIN_LOGIN" -> AdminLoginScreen(viewModel = viewModel)
            "ADMIN_DASHBOARD" -> AdminDashboardScreen(viewModel = viewModel)
            else -> LandingScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}
