package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HistoryItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HistoryWidget(
    historyItems: List<HistoryItem>,
    activeIndex: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val currentItem = historyItems.getOrNull(activeIndex)

    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassOverlay)
            .border(
                width = if (isFocused) 2.5.dp else 1.dp,
                color = if (isFocused) CyanAccent else GlassBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onInspect() }
            .testTag("history_widget_card")
    ) {
        val isCompact = maxWidth < 380.dp
        val padding = if (isCompact) 14.dp else 18.dp

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
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(if (isCompact) 18.dp else 20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.history_title),
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Item index pill
                if (historyItems.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.history_count_format, activeIndex + 1, historyItems.size),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Event content
            AnimatedContent(
                targetState = currentItem,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "history_event_transition"
            ) { item ->
                if (item != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Year Badge
                            Text(
                                text = item.year,
                                color = SunsetAmber,
                                fontSize = if (isCompact) 15.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFB300))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                    .testTag("history_year_badge")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = item.category.uppercase(),
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.text,
                            color = TextPrimary,
                            fontSize = if (isCompact) 13.sp else 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = if (isCompact) 18.sp else 20.sp,
                            maxLines = 3,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("history_event_text")
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.history_retrieving),
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer action bar: Prev / Next buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.click_read_full_timeline),
                    color = CyanAccent.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Row {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.previous_event),
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.next_event),
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
