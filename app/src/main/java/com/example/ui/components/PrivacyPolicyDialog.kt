package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@Composable
fun PrivacyPolicyDialog(
    onClose: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("privacy_dialog_scrim")
        ) {
            val isCompact = maxWidth < 440.dp
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("privacy_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .padding(if (isCompact) 16.dp else 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
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
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.privacy_policy_title),
                                color = TextPrimary,
                                fontSize = if (isCompact) 16.sp else 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.privacy_policy_content),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Commitment Cards
                    PrivacyBullet(
                        title = stringResource(R.string.privacy_bullet_zero_data_title),
                        desc = stringResource(R.string.privacy_bullet_zero_data_desc)
                    )
                    PrivacyBullet(
                        title = stringResource(R.string.privacy_bullet_open_apis_title),
                        desc = stringResource(R.string.privacy_bullet_open_apis_desc)
                    )
                    PrivacyBullet(
                        title = stringResource(R.string.privacy_bullet_local_storage_title),
                        desc = stringResource(R.string.privacy_bullet_local_storage_desc)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("privacy_ok_button")
                    ) {
                        Text(stringResource(R.string.privacy_policy_ok), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyBullet(title: String, desc: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(DarkSurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}
