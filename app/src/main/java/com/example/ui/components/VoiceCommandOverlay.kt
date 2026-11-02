package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.voice.VoiceCommandResult
import com.example.data.voice.VoiceState
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassOverlay
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceCommandOverlay(
    voiceState: VoiceState,
    rmsDb: Float,
    lastResult: VoiceCommandResult?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSimulateCommand: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var hasRecordAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasRecordAudioPermission = isGranted
        if (isGranted) {
            onStartListening()
        }
    }

    // Automatically trigger listening if permission is already granted when opened
    LaunchedEffect(hasRecordAudioPermission) {
        if (hasRecordAudioPermission && voiceState is VoiceState.Idle) {
            onStartListening()
        }
    }

    // Infinite pulsing wave animation for voice recording
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    // Dynamic scale influenced by real microphone audio RMS amplitude
    val audioReactiveScale = if (voiceState is VoiceState.Listening) {
        1f + (rmsDb.coerceIn(0f, 15f) / 15f) * 0.4f
    } else {
        1f
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
                .clickable { onClose() }
                .padding(16.dp)
                .testTag("voice_command_dialog_scrim"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 560.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                    .clickable(enabled = false) {}
                    .testTag("voice_command_dialog_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.voice_command_title),
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.voice_command_subtitle),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("voice_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Center Animated Microphone Visualizer Pod
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(140.dp)
                            .testTag("voice_mic_visualizer_container")
                    ) {
                        if (voiceState is VoiceState.Listening) {
                            // Pulsing Outer Aura Rings
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .scale(pulseScale * audioReactiveScale)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.12f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .scale(audioReactiveScale)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.22f))
                            )
                        }

                        // Core Mic Button
                        var isMicFocused by remember { mutableStateOf(false) }
                        val coreColor = when (voiceState) {
                            is VoiceState.Listening -> CyanAccent
                            is VoiceState.Processing -> SunsetAmber
                            is VoiceState.Success -> EmeraldGreen
                            is VoiceState.Error -> Color(0xFFEF5350)
                            else -> DarkSurfaceElevated
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(coreColor)
                                .border(
                                    width = if (isMicFocused) 3.dp else 1.dp,
                                    color = if (isMicFocused) Color.White else GlassBorder,
                                    shape = CircleShape
                                )
                                .focusable()
                                .onFocusChanged { isMicFocused = it.isFocused }
                                .clickable {
                                    if (!hasRecordAudioPermission) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (voiceState is VoiceState.Listening) {
                                            onStopListening()
                                        } else {
                                            onStartListening()
                                        }
                                    }
                                }
                                .testTag("voice_toggle_mic_button")
                        ) {
                            when (voiceState) {
                                is VoiceState.Listening -> {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = stringResource(R.string.voice_status_listening),
                                        tint = VoidBlack,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                                is VoiceState.Processing -> {
                                    CircularProgressIndicator(
                                        color = VoidBlack,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                is VoiceState.Success -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = VoidBlack,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                is VoiceState.Error -> {
                                    Icon(
                                        imageVector = Icons.Default.MicOff,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.MicNone,
                                        contentDescription = stringResource(R.string.voice_status_idle),
                                        tint = CyanAccent,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Text / Live Recognition Feedback
                    when (voiceState) {
                        is VoiceState.Listening -> {
                            Text(
                                text = stringResource(R.string.voice_status_listening),
                                color = CyanAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                        is VoiceState.Processing -> {
                            Text(
                                text = stringResource(R.string.voice_status_processing),
                                color = SunsetAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                        is VoiceState.Success -> {
                            val res = voiceState.result
                            val actionFeedback = if (res.targetCollection != null) {
                                stringResource(res.action.feedbackRes, stringResource(res.targetCollection.titleRes))
                            } else {
                                stringResource(res.action.feedbackRes)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "“${res.rawText}”",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ $actionFeedback",
                                    color = EmeraldGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        is VoiceState.Error -> {
                            Text(
                                text = stringResource(voiceState.messageRes),
                                color = Color(0xFFEF5350),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                        is VoiceState.Idle -> {
                            if (!hasRecordAudioPermission) {
                                Text(
                                    text = stringResource(R.string.voice_status_permission_needed),
                                    color = SunsetAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("voice_grant_permission_button")
                                ) {
                                    Text(stringResource(R.string.voice_grant_permission), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.voice_status_idle),
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Suggested Commands Quick Chips (Clickable / D-Pad focusable)
                    Text(
                        text = stringResource(R.string.voice_hint_suggestions),
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val sampleCommands = listOf(
                        stringResource(R.string.voice_cmd_sample_weather) to "toggle weather",
                        stringResource(R.string.voice_cmd_sample_radar) to "show flight radar",
                        stringResource(R.string.voice_cmd_sample_crypto) to "toggle crypto",
                        stringResource(R.string.voice_cmd_sample_mountains) to "mountains",
                        stringResource(R.string.voice_cmd_sample_aurora) to "aurora",
                        stringResource(R.string.voice_cmd_sample_next) to "next photo",
                        stringResource(R.string.voice_cmd_sample_screensaver) to "screensaver"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sampleCommands.forEach { (label, commandPhrase) ->
                            var isChipFocused by remember { mutableStateOf(false) }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(
                                        width = if (isChipFocused) 1.5.dp else 1.dp,
                                        color = if (isChipFocused) CyanAccent else DarkBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .focusable()
                                    .onFocusChanged { isChipFocused = it.isFocused }
                                    .clickable {
                                        onSimulateCommand(commandPhrase)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("sample_voice_cmd_${label.replace(' ', '_').lowercase()}")
                            ) {
                                Text(
                                    text = label,
                                    color = if (isChipFocused) CyanAccent else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
