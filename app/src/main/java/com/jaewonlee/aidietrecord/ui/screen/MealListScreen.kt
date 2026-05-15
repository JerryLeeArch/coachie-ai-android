package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.sampleMeals
import com.jaewonlee.aidietrecord.ui.util.formatMealDateTime
import com.jaewonlee.aidietrecord.ui.util.isTodayMeal

private enum class MealRecordFilter {
    Today,
    All
}

@Composable
fun MealListScreen(
    onBackClick: () -> Unit,
    onMealClick: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(MealRecordFilter.Today) }
    val displayedMeals = remember(selectedFilter) {
        when (selectedFilter) {
            MealRecordFilter.Today -> sampleMeals.filter { isTodayMeal(it.createdAt) }
            MealRecordFilter.All -> sampleMeals
        }
    }

    ScreenScaffold(
        title = "식단 기록",
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
            text = "오늘 기록 보기",
            selected = selectedFilter == MealRecordFilter.Today,
            onClick = { onFilterSelected(MealRecordFilter.Today) },
            modifier = Modifier.weight(1f)
        )
        FilterButton(
            text = "전체 기록 보기",
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
        MealRecordFilter.Today -> "오늘 기록이 없습니다."
        MealRecordFilter.All -> "저장된 기록이 없습니다."
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFEAF1E8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("사진", color = Color(0xFF52624F))
            }
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
                    text = formatMealDateTime(mealRecord.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF52624F)
                )
                Text("${mealRecord.calories} kcal", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "탄 ${mealRecord.carbsGram}g · 단 ${mealRecord.proteinGram}g · 지 ${mealRecord.fatGram}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF52624F)
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
