package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CryptoTicker
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.DecimalFormat

@Composable
fun CryptoTickerWidget(
    tickers: List<CryptoTicker>,
    onInspect: () -> Unit,
    isShort: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val focusScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "crypto_focus_scale"
    )

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .scale(focusScale)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassOverlay)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) SunsetAmber else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onInspect() }
            .testTag("crypto_ticker_widget_card")
    ) {
        val isCompact = maxWidth < 440.dp || isShort
        val padding = if (isShort) 6.dp else if (isCompact) 8.dp else 16.dp

        Column(modifier = Modifier.padding(padding)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyBitcoin,
                        contentDescription = stringResource(R.string.crypto_icon_desc),
                        tint = SunsetAmber,
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.crypto_title).uppercase(),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.streaming_realtime),
                    color = SunsetAmber.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(if (isShort || isCompact) 4.dp else 8.dp))

            // Horizontal Crypto Ticker Tape
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                tickers.forEach { ticker ->
                    CryptoMiniCard(ticker = ticker, isCompact = isCompact, isShort = isShort, onClick = onInspect)
                }
            }
        }
    }
}

@Composable
private fun CryptoMiniCard(
    ticker: CryptoTicker,
    isCompact: Boolean = false,
    isShort: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isPositive = ticker.changePercent24h >= 0
    val trendColor = if (isPositive) EmeraldGreen else CoralRed
    val priceFormatter = DecimalFormat("#,##0.00")
    val displayPrice = if (ticker.priceUsd < 1.0) "%.4f".format(ticker.priceUsd) else priceFormatter.format(ticker.priceUsd)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x22182234))
            .border(1.dp, DarkBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = if (isShort || isCompact) 8.dp else 10.dp, vertical = if (isShort || isCompact) 5.dp else 8.dp)
            .width(if (isShort) 130.dp else if (isCompact) 146.dp else 138.dp)
            .testTag("crypto_card_${ticker.symbol.lowercase()}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = ticker.symbol,
                        color = TextPrimary,
                        fontSize = if (isCompact) 13.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ticker.name,
                        color = TextMuted,
                        fontSize = if (isCompact) 9.sp else 10.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 24h Change badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(trendColor.copy(alpha = 0.15f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "%+.2f%%".format(ticker.changePercent24h),
                        color = trendColor,
                        fontSize = if (isCompact) 9.5.sp else 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Price and Canvas Sparkline Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.usd_price_format, displayPrice),
                    color = TextPrimary,
                    fontSize = if (isCompact) 12.5.sp else 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Mini Canvas Sparkline with glowing gradient underlay
                if (ticker.sparkline.isNotEmpty()) {
                    Canvas(modifier = Modifier.size(width = if (isCompact) 38.dp else 40.dp, height = if (isCompact) 18.dp else 20.dp)) {
                        val path = Path()
                        val stepX = size.width / (ticker.sparkline.size - 1)
                        ticker.sparkline.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = (1f - pt) * (size.height - 4f) + 2f
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Gradient fill under the curve
                        val fillPath = Path()
                        fillPath.addPath(path)
                        fillPath.lineTo(size.width, size.height)
                        fillPath.lineTo(0f, size.height)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(trendColor.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = size.height
                            )
                        )

                        drawPath(
                            path = path,
                            color = trendColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
