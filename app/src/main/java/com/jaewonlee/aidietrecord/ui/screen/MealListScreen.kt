package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.util.UriImage
import com.jaewonlee.aidietrecord.ui.util.formatMealDateTime
import com.jaewonlee.aidietrecord.ui.util.isTodayMeal

private enum class MealRecordFilter {
    Today,
    All
}

@Composable
fun MealListScreen(
    mealRecords: List<MealRecord>,
    onBackClick: () -> Unit,
    onMealClick: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(MealRecordFilter.Today) }
    val displayedMeals = remember(selectedFilter, mealRecords) {
        when (selectedFilter) {
            MealRecordFilter.Today -> mealRecords.filter { isTodayMeal(it) }
            MealRecordFilter.All -> mealRecords
        }
    }

    ScreenScaffold(
        title = "Meal History",
        onBackClick = onBackClick
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MealRecordFilterButtons(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }
            if (displayedMeals.isEmpty()) {
                item {
                    EmptyMealRecordMessage(selectedFilter = selectedFilter)
                }
            } else {
                items(displayedMeals) { meal ->
                    MealListItem(
                        mealRecord = meal,
                        onClick = { onMealClick(meal.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MealRecordFilterButtons(
    selectedFilter: MealRecordFilter,
    onFilterSelected: (MealRecordFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton(
            text = "Today",
            selected = selectedFilter == MealRecordFilter.Today,
            onClick = { onFilterSelected(MealRecordFilter.Today) },
            modifier = Modifier.weight(1f)
        )
        FilterButton(
            text = "All Records",
            selected = selectedFilter == MealRecordFilter.All,
            onClick = { onFilterSelected(MealRecordFilter.All) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    }
}

@Composable
private fun EmptyMealRecordMessage(selectedFilter: MealRecordFilter) {
    val message = when (selectedFilter) {
        MealRecordFilter.Today -> "No meals logged today."
        MealRecordFilter.All -> "No meal records saved."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppOutline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Composable
private fun MealListItem(
    mealRecord: MealRecord,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppOutline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UriImage(
                imageUri = mealRecord.imageUri,
                placeholderText = "Photo",
                modifier = Modifier.size(72.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = mealRecord.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatMealDateTime(mealRecord),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTextMuted
                )
                Text("${mealRecord.calories} kcal", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Carbs ${mealRecord.carbsGram}g | Protein ${mealRecord.proteinGram}g | Fat ${mealRecord.fatGram}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTextMuted
                )
                Text(
                    text = "Fiber ${mealRecord.fiberGram}g | Sugar ${mealRecord.sugarGram}g | Sodium ${mealRecord.sodiumMilligram}mg",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTextMuted
                )
                Text(
                    text = mealRecord.memo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
