package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.LandscapeCollection
import com.example.data.model.WallpaperItem
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArtGalleryDialog(
    currentWallpaper: WallpaperItem?,
    wallpapers: List<WallpaperItem>,
    wallpaperIndex: Int,
    selectedCollection: LandscapeCollection,
    wallpaperIntervalSec: Int,
    isFetchingArt: Boolean,
    onSelectCollection: (LandscapeCollection) -> Unit,
    onCycleCollection: () -> Unit,
    onNextWallpaper: () -> Unit,
    onPrevWallpaper: () -> Unit,
    onFetchFresh: () -> Unit,
    onSelectInterval: (Int) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xDD000000))
                .padding(10.dp)
                .testTag("art_gallery_dialog_scrim")
        ) {
            val isCompact = maxWidth < 460.dp
            val isShort = maxHeight < 620.dp
            val maxAllowedHeight = if (isShort) maxHeight * 0.95f else maxHeight * 0.90f

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(if (isCompact) 0.98f else 0.88f)
                    .widthIn(max = 840.dp)
                    .heightIn(max = maxAllowedHeight)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("art_gallery_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isShort) 12.dp else if (isCompact) 14.dp else 22.dp)
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
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(if (isCompact) 32.dp else 38.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.art_gallery_title),
                                    color = TextPrimary,
                                    fontSize = if (isCompact) 15.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = stringResource(R.string.art_gallery_subtitle),
                                    color = TextSecondary,
                                    fontSize = if (isCompact) 10.sp else 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("art_gallery_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isShort) 8.dp else 14.dp))

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Current Active Wallpaper Spotlight Showcase Card
                        if (currentWallpaper != null) {
                            ActivePhotoShowcase(
                                wallpaper = currentWallpaper,
                                totalInCollection = wallpapers.size,
                                currentIndex = wallpaperIndex,
                                isCompact = isCompact,
                                isShort = isShort,
                                isFetchingArt = isFetchingArt,
                                onNext = onNextWallpaper,
                                onPrev = onPrevWallpaper,
                                onCycleCollection = onCycleCollection,
                                onFetchFresh = onFetchFresh
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // 2. Landscape Collections Section
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Landscape, contentDescription = null, tint = SunsetAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.collections_label),
                                    color = SunsetAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            // Cycle collection quick button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onCycleCollection() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("btn_cycle_collection_header")
                            ) {
                                Icon(Icons.Default.Autorenew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.cycle_collection),
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Collections Grid (2 columns on wide, 1 column on compact)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LandscapeCollection.values().forEach { col ->
                                val isSelected = selectedCollection == col
                                CollectionItemCard(
                                    collection = col,
                                    isSelected = isSelected,
                                    isCompact = isCompact,
                                    onClick = { onSelectCollection(col) },
                                    modifier = Modifier.weight(if (isCompact) 1f else 0.48f, fill = true)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Rotation Interval Controls
                        Text(
                            text = stringResource(R.string.rotation_interval).uppercase(),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val intervals = listOf(
                            30 to stringResource(R.string.sec_30),
                            60 to stringResource(R.string.sec_60),
                            180 to stringResource(R.string.sec_180),
                            300 to stringResource(R.string.sec_300),
                            600 to stringResource(R.string.sec_600)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            intervals.forEach { (sec, label) ->
                                val isChosen = wallpaperIntervalSec == sec
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) CyanAccent.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(1.dp, if (isChosen) CyanAccent else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { onSelectInterval(sec) }
                                        .padding(vertical = 8.dp)
                                        .testTag("interval_chip_$sec")
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isChosen) CyanAccent else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivePhotoShowcase(
    wallpaper: WallpaperItem,
    totalInCollection: Int,
    currentIndex: Int,
    isCompact: Boolean,
    isShort: Boolean,
    isFetchingArt: Boolean,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onCycleCollection: () -> Unit,
    onFetchFresh: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("active_photo_showcase_card")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Photo Preview with cinematic aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isShort) 21f / 9f else 16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VoidBlack)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wallpaper.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom vignette gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC000000)),
                                startY = 100f
                            )
                        )
                )

                // Unsplash Attribution Badge (Top Right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stringResource(R.string.unsplash_royalty_free),
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Title & Author Overlay (Bottom Left)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = wallpaper.title,
                        color = TextPrimary,
                        fontSize = if (isCompact) 13.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.unsplash_attribution_format, wallpaper.author),
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Collection Count Pill (Bottom Right)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xAA000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${currentIndex + 1} / $totalInCollection",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onPrev,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .testTag("gallery_prev_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.prev_photo),
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .testTag("gallery_next_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.next_photo),
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Fetch fresh photos
                    OutlinedButton(
                        onClick = onFetchFresh,
                        enabled = !isFetchingArt,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("gallery_fetch_fresh_btn")
                    ) {
                        if (isFetchingArt) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = CyanAccent)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.fetch_fresh_unsplash), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Cycle collection
                    Button(
                        onClick = onCycleCollection,
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = VoidBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp).testTag("gallery_cycle_collection_btn")
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.cycle_collection), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionItemCard(
    collection: LandscapeCollection,
    isSelected: Boolean,
    isCompact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CyanAccent.copy(alpha = 0.15f) else DarkSurfaceElevated)
            .border(
                width = if (isFocused) 2.5.dp else if (isSelected) 1.8.dp else 1.dp,
                color = if (isFocused) EmeraldGreen else if (isSelected) CyanAccent else DarkBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(12.dp)
            .testTag("collection_card_${collection.name}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(collection.titleRes),
                    color = if (isSelected) CyanAccent else TextPrimary,
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyanAccent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = stringResource(collection.descRes),
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.photos_count_format, 4),
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
