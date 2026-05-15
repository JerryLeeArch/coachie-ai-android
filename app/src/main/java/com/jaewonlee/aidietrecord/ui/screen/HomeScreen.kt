package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.sampleMeals
import com.jaewonlee.aidietrecord.ui.util.isTodayMeal

private val CarbColor = Color(0xFF3B82C4)
private val ProteinColor = Color(0xFF2D6A4F)
private val FatColor = Color(0xFFD18B2F)

@Composable
fun HomeScreen(
    nickname: String,
    targetCalories: Int,
    targetProteinGram: Int,
    onAddMealClick: () -> Unit,
    onMealListClick: () -> Unit,
    onGoalSettingsClick: () -> Unit,
    onRecentStatsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val todayMeals = sampleMeals.filter { isTodayMeal(it.createdAt) }
    val totalCalories = todayMeals.sumOf { it.calories }
    val totalCarbs = todayMeals.sumOf { it.carbsGram }
    val totalProtein = todayMeals.sumOf { it.proteinGram }
    val totalFat = todayMeals.sumOf { it.fatGram }
    val targetCarbs = 250
    val targetFat = 60
    val progress = (totalCalories / targetCalories.toFloat()).coerceIn(0f, 1f)

    ScreenScaffold(
        title = "AI 식단 기록",
        actions = {
            ProfileActionButton(
                label = profileInitial(nickname),
                onClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "오늘의 식단 요약",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("섭취 칼로리", style = MaterialTheme.typography.titleMedium)
                        OutlinedButton(onClick = onMealListClick) {
                            Text("기록 보기")
                        }
                    }
                    Text(
                        text = "$totalCalories kcal / $targetCalories kcal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = Color(0xFFE0E7DE)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryChip(label = "기록", value = "${todayMeals.size}개")
                        SummaryChip(label = "남은 칼로리", value = "${targetCalories - totalCalories} kcal")
                    }
                    NutritionComposition(
                        carbsGram = totalCarbs,
                        proteinGram = totalProtein,
                        fatGram = totalFat,
                        targetCarbsGram = targetCarbs,
                        targetProteinGram = targetProteinGram,
                        targetFatGram = targetFat
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecentStatsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stats")
                }
                Button(
                    onClick = onAddMealClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add")
                }
                OutlinedButton(
                    onClick = onGoalSettingsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Goal")
                }
            }
        }
    }
}

@Composable
private fun NutritionComposition(
    carbsGram: Int,
    proteinGram: Int,
    fatGram: Int,
    targetCarbsGram: Int,
    targetProteinGram: Int,
    targetFatGram: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "영양분 구성",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        MacroSegmentBar(
            carbsGram = carbsGram,
            proteinGram = proteinGram,
            fatGram = fatGram
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroLegend("탄수", carbsGram, CarbColor, Modifier.weight(1f))
            MacroLegend("단백질", proteinGram, ProteinColor, Modifier.weight(1f))
            MacroLegend("지방", fatGram, FatColor, Modifier.weight(1f))
        }
        NutritionTargetRow("탄수화물", carbsGram, targetCarbsGram, CarbColor)
        NutritionTargetRow("단백질", proteinGram, targetProteinGram, ProteinColor)
        NutritionTargetRow("지방", fatGram, targetFatGram, FatColor)
    }
}

@Composable
private fun MacroSegmentBar(
    carbsGram: Int,
    proteinGram: Int,
    fatGram: Int
) {
    val carbCalories = carbsGram * 4
    val proteinCalories = proteinGram * 4
    val fatCalories = fatGram * 9

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFE8ECE6))
    ) {
        if (carbCalories > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(carbCalories.toFloat())
                    .background(CarbColor)
            )
        }
        if (proteinCalories > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(proteinCalories.toFloat())
                    .background(ProteinColor)
            )
        }
        if (fatCalories > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(fatCalories.toFloat())
                    .background(FatColor)
            )
        }
    }
}

@Composable
private fun MacroLegend(
    label: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label ${grams}g",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NutritionTargetRow(
    label: String,
    grams: Int,
    targetGrams: Int,
    color: Color
) {
    val progress = (grams / targetGrams.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = "${grams}g / ${targetGrams}g",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = Color(0xFFE8ECE6)
        )
    }
}

@Composable
private fun ProfileActionButton(
    label: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(40.dp)
            .background(Color(0xFFEFF6EE), CircleShape)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D6A4F)
        )
    }
}

private fun profileInitial(nickname: String): String {
    return nickname.trim().firstOrNull()?.toString() ?: "내"
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF6EE), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF52624F))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}
