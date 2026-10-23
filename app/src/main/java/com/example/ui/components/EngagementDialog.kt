package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.manager.PromptType
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun EngagementDialog(
    promptType: PromptType,
    onRateApp: () -> Unit,
    onRateLater: () -> Unit,
    onRateNever: () -> Unit,
    onShareApp: () -> Unit,
    onDismissShare: () -> Unit,
    onUpdateApp: () -> Unit,
    onDismissUpdate: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            when (promptType) {
                PromptType.RATE -> onRateLater()
                PromptType.SHARE -> onDismissShare()
                PromptType.UPDATE -> onDismissUpdate()
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("engagement_dialog_scrim")
        ) {
            val isCompact = maxWidth < 420.dp
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("engagement_dialog_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(if (isCompact) 18.dp else 28.dp)
                ) {
                    when (promptType) {
                        PromptType.RATE -> {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = SunsetAmber,
                                modifier = Modifier.size(if (isCompact) 40.dp else 48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.rate_dialog_title),
                                color = TextPrimary,
                                fontSize = if (isCompact) 18.sp else 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            // 5 Stars visual
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SunsetAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.rate_dialog_desc),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onRateApp,
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetAmber, contentColor = VoidBlack),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("rate_accept_button")
                            ) {
                                Text(stringResource(R.string.rate_now), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(onClick = onRateNever) {
                                    Text(stringResource(R.string.rate_never), color = TextMuted, fontSize = 12.sp)
                                }
                                TextButton(onClick = onRateLater) {
                                    Text(stringResource(R.string.remind_later), color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        PromptType.UPDATE -> {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.update_dialog_title),
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.update_dialog_desc),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onUpdateApp,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("update_accept_button")
                            ) {
                                Text(stringResource(R.string.update_now), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onDismissUpdate) {
                                Text(stringResource(R.string.remind_later), color = TextMuted, fontSize = 12.sp)
                            }
                        }

                        PromptType.SHARE -> {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.share_dialog_title),
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.share_dialog_desc),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = onShareApp,
                                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("share_accept_button")
                            ) {
                                Text(stringResource(R.string.share_now), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onDismissShare) {
                                Text(stringResource(R.string.maybe_later), color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
