package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.FishGlyph
import org.example.project.components.GlassCard
import org.example.project.components.InfoDialog
import org.example.project.components.PrimaryButton
import org.example.project.components.SecondaryButton
import org.example.project.components.UriImage
import org.example.project.data.FishItem
import org.example.project.data.rememberFishPhoto
import org.example.project.media.MediaDenialReason
import org.example.project.media.rememberMediaController
import org.example.project.theme.AquariumColors

@Composable
fun FishDetailScreen(
    fish: FishItem,
    onBack: () -> Unit,
) {
    val photo = rememberFishPhoto(fish.name)
    val media = rememberMediaController()
    var dialogTitle by remember { mutableStateOf<String?>(null) }
    var dialogMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        DetailTopBar(
            title = fish.name,
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HeroImage(fish = fish, photoUri = photo.uri)
            PhotoActionsRow(
                hasPhoto = photo.uri != null,
                onTakePhoto = {
                    media.takePhoto(
                        onResult = { result -> result?.uri?.let { photo.set(it) } },
                        onDenied = { reason ->
                            when (reason) {
                                MediaDenialReason.CameraPermissionDenied -> {
                                    dialogTitle = "Camera unavailable"
                                    dialogMessage = "The app cannot access the camera. Photo capture is disabled."
                                }
                                MediaDenialReason.CameraUnavailable -> {
                                    dialogTitle = "Camera unavailable"
                                    dialogMessage = "This device does not seem to have a camera available."
                                }
                                else -> Unit
                            }
                        },
                    )
                },
                onChooseFromGallery = {
                    media.pickFromGallery(
                        onResult = { result -> result?.uri?.let { photo.set(it) } },
                        onDenied = { reason ->
                            if (reason == MediaDenialReason.GalleryPermissionDenied) {
                                dialogTitle = "Gallery unavailable"
                                dialogMessage = "The app cannot access the photo library."
                            }
                        },
                    )
                },
                onDeletePhoto = { photo.clear() },
            )
            CategoryChips(fish = fish)
            CharacteristicsCard(fish = fish)
            if (fish.description.isNotBlank()) {
                DescriptionCard(text = fish.description)
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (dialogTitle != null) {
        InfoDialog(
            title = dialogTitle!!,
            message = dialogMessage,
            onDismiss = { dialogTitle = null },
        )
    }
}

@Composable
private fun DetailTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AquariumColors.GlassBlueStrong)
                .border(1.dp, AquariumColors.Stroke, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "‹",
                color = AquariumColors.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = title,
            color = AquariumColors.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HeroImage(fish: FishItem, photoUri: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        fish.accent.copy(alpha = 0.55f),
                        AquariumColors.MidOcean.copy(alpha = 0.7f),
                    )
                )
            )
            .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            UriImage(uri = photoUri, modifier = Modifier.fillMaxSize())
        } else {
            FishGlyph(color = fish.accent, modifier = Modifier.fillMaxSize().padding(24.dp))
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(
                    AquariumColors.DeepOcean.copy(alpha = 0.6f),
                    RoundedCornerShape(50),
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = fish.type,
                color = AquariumColors.PaleAqua,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PhotoActionsRow(
    hasPhoto: Boolean,
    onTakePhoto: () -> Unit,
    onChooseFromGallery: () -> Unit,
    onDeletePhoto: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SecondaryButton(
            text = "Take photo",
            onClick = onTakePhoto,
            leadingGlyph = "◉",
            modifier = Modifier.weight(1f),
        )
        SecondaryButton(
            text = "From gallery",
            onClick = onChooseFromGallery,
            leadingGlyph = "❒",
            modifier = Modifier.weight(1f),
        )
    }
    if (hasPhoto) {
        PrimaryButton(
            text = "Remove photo",
            onClick = onDeletePhoto,
            leadingGlyph = "✕",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CategoryChips(fish: FishItem) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip(text = fish.category.label)
        Chip(text = "Care: ${fish.careLevel}")
        Chip(text = "Compatibility: ${fish.compatibility}/3")
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(AquariumColors.GlassBlueStrong, RoundedCornerShape(50))
            .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            color = AquariumColors.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CharacteristicsCard(fish: FishItem) {
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Characteristics",
                color = AquariumColors.MutedAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            CharacteristicRow("Size", fish.sizeCm)
            CharacteristicRow("Temperature", fish.tempC)
            CharacteristicRow("pH", fish.phRange)
            CharacteristicRow("Lifespan", fish.lifespanYears)
            CharacteristicRow("Diet", fish.diet)
            CharacteristicRow("Origin", fish.origin)
        }
    }
}

@Composable
private fun CharacteristicRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = AquariumColors.PaleAqua,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            color = AquariumColors.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DescriptionCard(text: String) {
    GlassCard(modifier = Modifier.fillMaxWidth(), padding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "About",
                color = AquariumColors.MutedAqua,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = text,
                color = AquariumColors.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}
