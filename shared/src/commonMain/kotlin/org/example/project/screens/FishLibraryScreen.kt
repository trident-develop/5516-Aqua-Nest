package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.FishCard
import org.example.project.components.SectionHeader
import org.example.project.components.SystemBackHandler
import org.example.project.data.FishItem
import org.example.project.data.SampleData
import org.example.project.data.rememberFishPhoto
import org.example.project.theme.AquariumColors

private val categories = listOf("All", "Freshwater", "Saltwater", "Peaceful", "Sensitive")

@Composable
fun FishLibraryScreen() {
    var selectedFishName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedFish: FishItem? = remember(selectedFishName) {
        selectedFishName?.let { name -> SampleData.fish.firstOrNull { it.name == name } }
    }

    if (selectedFish != null) {
        // System back closes the detail screen first before any outer handler.
        SystemBackHandler(enabled = true) { selectedFishName = null }
        FishDetailScreen(fish = selectedFish, onBack = { selectedFishName = null })
    } else {
        FishCatalog(onOpenFish = { selectedFishName = it.name })
    }
}

@Composable
private fun FishCatalog(onOpenFish: (FishItem) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    val focusManager = LocalFocusManager.current

    val filtered = remember(query, selectedCategory) {
        SampleData.fish.filter { fish ->
            val matchesQuery = query.isBlank() ||
                fish.name.contains(query, ignoreCase = true) ||
                fish.type.contains(query, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                else -> fish.category.label == selectedCategory
            }
            matchesQuery && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                HeaderBlock()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SearchField(
                    query = query,
                    onChange = { query = it },
                    onSubmit = { focusManager.clearFocus() },
                )
                CategoryFilters(
                    selected = selectedCategory,
                    onSelect = {
                        selectedCategory = it
                        focusManager.clearFocus()
                    }
                )
                SectionHeader(title = "Catalog (${filtered.size})")
                FishGrid(
                    items = filtered,
                    modifier = Modifier.weight(1f),
                    onClickFish = { fish ->
                        focusManager.clearFocus()
                        onOpenFish(fish)
                    },
                )
            }
        }
    }
}

@Composable
private fun HeaderBlock() {
    Column {
        Text(
            text = "Fish library",
            color = AquariumColors.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap a fish for details and photo",
            color = AquariumColors.PaleAqua,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AquariumColors.GlassBlueStrong, RoundedCornerShape(18.dp))
            .border(1.dp, AquariumColors.Stroke, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⌕",
                color = AquariumColors.PaleAqua,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 10.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search by fish name…",
                        color = AquariumColors.MutedAqua,
                        fontSize = 14.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = AquariumColors.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(AquariumColors.Lime),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            onChange("")
                            onSubmit()
                        }
                ) {
                    Text(
                        text = "✕",
                        color = AquariumColors.PaleAqua,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilters(selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { category ->
            val active = category == selected
            Box(
                modifier = Modifier
                    .background(
                        if (active) AquariumColors.Lime else AquariumColors.GlassBlueStrong,
                        RoundedCornerShape(50)
                    )
                    .border(
                        1.dp,
                        if (active) AquariumColors.Lime else AquariumColors.Stroke,
                        RoundedCornerShape(50)
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    color = if (active) AquariumColors.DeepOcean else AquariumColors.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FishGrid(
    items: List<FishItem>,
    modifier: Modifier = Modifier,
    onClickFish: (FishItem) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(items, key = { it.name }) { fish ->
            val photo = rememberFishPhoto(fish.name)
            FishCard(
                fish = fish,
                photoUri = photo.uri,
                onClick = { onClickFish(fish) },
            )
        }
    }
}
