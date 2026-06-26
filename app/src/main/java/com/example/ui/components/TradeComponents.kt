package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.random.Random

// Reusable premium glassmorphism card
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderGlow: Boolean = true,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (borderGlow) BorderColor else Color(0x11FFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            content()
        }
    }
}

// Sparkline Mini Line charts for list items
@Composable
fun SparklineChart(
    prices: List<Float>,
    modifier: Modifier = Modifier.size(90.dp, 35.dp),
    lineColor: Color = SuccessGreen
) {
    if (prices.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val minPrice = prices.minOrNull() ?: 0f
        val maxPrice = prices.maxOrNull() ?: 1f
        val priceRange = if (maxPrice - minPrice == 0f) 1f else maxPrice - minPrice

        val path = Path()
        val stepX = width / (prices.size - 1)

        prices.forEachIndexed { index, price ->
            val x = index * stepX
            val y = height - ((price - minPrice) / priceRange) * height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// Enterprise Candlestickers chart powered by live interactive TradingView Widget embed
@Composable
fun MainCandlestickChart(
    symbol: String,
    currentPrice: Double,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
) {
    val tvSymbol = remember(symbol) {
        when (symbol.uppercase()) {
            "BTC/USDT" -> "BINANCE:BTCUSDT"
            "ETH/USDT" -> "BINANCE:ETHUSDT"
            "SOL/USDT" -> "BINANCE:SOLUSDT"
            "XRP/USDT" -> "BINANCE:XRPUSDT"
            "EUR/USD" -> "FX:EURUSD"
            "GBP/USD" -> "FX:GBPUSD"
            "USD/JPY" -> "FX:USDJPY"
            "AUD/USD" -> "FX:AUDUSD"
            "NASDAQ" -> "NASDAQ:NDX"
            "S&P500" -> "SP:SPX"
            "DOW JONES" -> "DJ:DJI"
            "GOLD" -> "TVC:GOLD"
            "SILVER" -> "TVC:SILVER"
            "CRUDE_OIL" -> "TVC:UKOIL"
            else -> symbol.replace("/", "")
        }
    }

    val htmlContent = remember(tvSymbol) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <style>
                html, body {
                    margin: 0;
                    padding: 0;
                    height: 100%;
                    width: 100%;
                    background-color: #080808;
                    overflow: hidden;
                }
                #tradingview_wrapper {
                    height: 100%;
                    width: 100%;
                }
            </style>
        </head>
        <body>
            <div id="tradingview_wrapper">
                <div id="tradingview_chart" style="height: 100%; width: 100%;"></div>
                <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                <script type="text/javascript">
                new TradingView.widget({
                    "autosize": true,
                    "symbol": "$tvSymbol",
                    "interval": "60",
                    "timezone": "Etc/UTC",
                    "theme": "dark",
                    "style": "1",
                    "locale": "en",
                    "toolbar_bg": "#080808",
                    "enable_publishing": false,
                    "hide_side_toolbar": true,
                    "allow_symbol_change": false,
                    "container_id": "tradingview_chart",
                    "studies": [],
                    "show_popup_button": false,
                    "withdateranges": true,
                    "save_image": false,
                    "backgroundColor": "#080808",
                    "gridColor": "rgba(42, 46, 57, 0.08)",
                    "drawings_access": { "type": "black", "tools": [] }
                });
                </script>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    Box(
        modifier = modifier
            .background(Color(0xFF080808))
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(1.dp)
    ) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.webkit.WebView(context).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        databaseEnabled = true
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = android.webkit.WebChromeClient()
                    setBackgroundColor(android.graphics.Color.parseColor("#080808"))
                    loadDataWithBaseURL("https://s3.tradingview.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                val tag = webView.tag as? String
                if (tag != tvSymbol) {
                    webView.tag = tvSymbol
                    webView.loadDataWithBaseURL("https://s3.tradingview.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val isGreen: Boolean
)
