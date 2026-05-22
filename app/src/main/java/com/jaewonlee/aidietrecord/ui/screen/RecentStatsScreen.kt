package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.util.formatMealDate
import com.jaewonlee.aidietrecord.ui.util.mealRecordDate
import java.time.LocalDate

@Composable
fun RecentStatsScreen(
    mealRecords: List<MealRecord>,
    targetCalories: Int,
    targetProteinGram: Int,
    onBackClick: () -> Unit
) {
    val dailyStats = remember(mealRecords) { mealRecords.toDailyStats() }
    val recentStats = dailyStats.take(7)
    val latestStats = recentStats.firstOrNull()
    val previousStats = recentStats.drop(1).firstOrNull()
    val averageCalories = recentStats.averageOf { it.calories }
    val averageProtein = recentStats.averageOf { it.proteinGram }
    val averageRecords = recentStats.averageOf { it.recordCount }

    ScreenScaffold(
        title = "Insights",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Recent Day Averages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatsChip("Calories", "${averageCalories} kcal", Modifier.weight(1f))
                        StatsChip("Protein", "${averageProtein}g", Modifier.weight(1f))
                        StatsChip("Records", "${averageRecords}", Modifier.weight(1f))
                    }
                    Text(
                        text = buildTrendText(latestStats, previousStats),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2D6A4F)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Calorie Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    recentStats.forEach { stats ->
                        DailyCalorieRow(
                            stats = stats,
                            targetCalories = targetCalories
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nutrition Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    recentStats.forEach { stats ->
                        DailyMacroRow(
                            stats = stats,
                            targetProteinGram = targetProteinGram
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFEFF6EE), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF52624F))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DailyCalorieRow(
    stats: DailyNutritionStats,
    targetCalories: Int
) {
    val progress = (stats.calories / targetCalories.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatMealDate(stats.date), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${stats.calories} kcal / ${targetCalories} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            trackColor = Color(0xFFE8ECE6)
        )
    }
}

@Composable
private fun DailyMacroRow(
    stats: DailyNutritionStats,
    targetProteinGram: Int
) {
    val proteinProgress = (stats.proteinGram / targetProteinGram.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatMealDate(stats.date), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Carbs ${stats.carbsGram}g | Protein ${stats.proteinGram}g | Fat ${stats.fatGram}g",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF52624F)
            )
        }
        LinearProgressIndicator(
            progress = { proteinProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = Color(0xFF2D6A4F),
            trackColor = Color(0xFFE8ECE6)
        )
    }
}

private data class DailyNutritionStats(
    val date: LocalDate,
    val recordCount: Int,
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int
)

private fun List<MealRecord>.toDailyStats(): List<DailyNutritionStats> {
    return groupBy { mealRecordDate(it.createdAt) }
        .map { (date, meals) ->
            DailyNutritionStats(
                date = date,
                recordCount = meals.size,
                calories = meals.sumOf { it.calories },
                carbsGram = meals.sumOf { it.carbsGram },
                proteinGram = meals.sumOf { it.proteinGram },
                fatGram = meals.sumOf { it.fatGram }
            )
        }
        .sortedByDescending { it.date }
}

private fun List<DailyNutritionStats>.averageOf(selector: (DailyNutritionStats) -> Int): Int {
    if (isEmpty()) return 0
    return map(selector).average().toInt()
}

private fun buildTrendText(
    latestStats: DailyNutritionStats?,
    previousStats: DailyNutritionStats?
): String {
    if (latestStats == null) return "No records yet to build insights."
    if (previousStats == null) return "Add more records to compare with your previous logged day."

    val calorieDiff = latestStats.calories - previousStats.calories
    return when {
        calorieDiff > 0 -> "You ate ${calorieDiff} kcal more than the previous logged day."
        calorieDiff < 0 -> "You ate ${-calorieDiff} kcal less than the previous logged day."
        else -> "You ate the same calories as the previous logged day."
    }
}
