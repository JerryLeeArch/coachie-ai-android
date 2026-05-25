package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.util.formatMealDate
import com.jaewonlee.aidietrecord.ui.util.mealRecordDate
import java.time.LocalDate
import kotlin.math.max

@Composable
fun RecentStatsScreen(
    mealRecords: List<MealRecord>,
    goalPlans: List<GoalPlanEntity>,
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
                    CalorieTrendChart(
                        stats = recentStats,
                        goalPlans = goalPlans,
                        fallbackTargetCalories = targetCalories
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
                        text = "Nutrition Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    NutritionTrendChart(
                        stats = recentStats,
                        goalPlans = goalPlans,
                        fallbackTargetProteinGram = targetProteinGram
                    )
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
private fun CalorieTrendChart(
    stats: List<DailyNutritionStats>,
    goalPlans: List<GoalPlanEntity>,
    fallbackTargetCalories: Int
) {
    if (stats.isEmpty()) {
        EmptyChartText("No calorie records yet.")
        return
    }

    val points = stats.asReversed().map { dailyStats ->
        val targetCalories = goalPlans.goalFor(dailyStats.date)
            ?.dailyCalories
            ?: fallbackTargetCalories
        CalorieChartPoint(
            date = dailyStats.date,
            calories = dailyStats.calories,
            targetCalories = targetCalories.coerceAtLeast(1)
        )
    }
    val chartMax = points.maxOf { max(it.calories, it.targetCalories) }
        .coerceAtLeast(1)
        .let { (it * 1.15f).coerceAtLeast(100f) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
        ) {
            val leftPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val gridColor = Color(0xFFE2E8DF)
            val targetColor = Color(0xFF9AA39A)
            val calorieColor = Color(0xFF5269A6)

            fun x(index: Int): Float {
                return if (points.size == 1) {
                    leftPadding + chartWidth / 2f
                } else {
                    leftPadding + chartWidth * (index / (points.lastIndex).toFloat())
                }
            }

            fun y(value: Int): Float {
                val ratio = (value / chartMax).coerceIn(0f, 1f)
                return topPadding + chartHeight * (1f - ratio)
            }

            repeat(4) { index ->
                val lineY = topPadding + chartHeight * (index / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, lineY),
                    end = Offset(leftPadding + chartWidth, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].targetCalories))
                },
                color = targetColor,
                strokeWidth = 2.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].calories))
                },
                color = calorieColor,
                strokeWidth = 4.dp.toPx()
            )
        }

        ChartDateLabels(points.first().date, points.last().date)

        val latestPoint = points.last()
        Text(
            text = "Latest: ${latestPoint.calories} / ${latestPoint.targetCalories} kcal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        ChartLegend(
            items = listOf(
                ChartLegendItem("Intake", Color(0xFF5269A6)),
                ChartLegendItem("Goal", Color(0xFF9AA39A))
            )
        )
    }
}

@Composable
private fun NutritionTrendChart(
    stats: List<DailyNutritionStats>,
    goalPlans: List<GoalPlanEntity>,
    fallbackTargetProteinGram: Int
) {
    if (stats.isEmpty()) {
        EmptyChartText("No nutrition records yet.")
        return
    }

    val points = stats.asReversed().map { dailyStats ->
        val goalPlan = goalPlans.goalFor(dailyStats.date)
        val targetCarbsGram = goalPlan?.dailyCarbsGram ?: 250
        val targetProteinGram = goalPlan?.dailyProteinGram ?: fallbackTargetProteinGram
        val targetFatGram = goalPlan?.dailyFatGram ?: 60

        MacroChartPoint(
            date = dailyStats.date,
            carbsGram = dailyStats.carbsGram,
            targetCarbsGram = targetCarbsGram.coerceAtLeast(1),
            proteinGram = dailyStats.proteinGram,
            targetProteinGram = targetProteinGram.coerceAtLeast(1),
            fatGram = dailyStats.fatGram,
            targetFatGram = targetFatGram.coerceAtLeast(1)
        )
    }
    val chartMaxRatio = points.maxOf {
        max(it.carbsProgress, max(it.proteinProgress, it.fatProgress))
    }
        .coerceAtLeast(1f)
        .let { (it * 1.15f).coerceAtMost(2f) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
        ) {
            val leftPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val gridColor = Color(0xFFE2E8DF)
            val targetColor = Color(0xFF9AA39A)
            val carbColor = Color(0xFF3F7FC2)
            val proteinColor = Color(0xFF2D6A4F)
            val fatColor = Color(0xFFD18B2F)

            fun x(index: Int): Float {
                return if (points.size == 1) {
                    leftPadding + chartWidth / 2f
                } else {
                    leftPadding + chartWidth * (index / (points.lastIndex).toFloat())
                }
            }

            fun y(progress: Float): Float {
                val ratio = (progress / chartMaxRatio).coerceIn(0f, 1f)
                return topPadding + chartHeight * (1f - ratio)
            }

            repeat(4) { index ->
                val lineY = topPadding + chartHeight * (index / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, lineY),
                    end = Offset(leftPadding + chartWidth, lineY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val targetY = y(1f)
            drawLine(
                color = targetColor,
                start = Offset(leftPadding, targetY),
                end = Offset(leftPadding + chartWidth, targetY),
                strokeWidth = 2.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].carbsProgress))
                },
                color = carbColor,
                strokeWidth = 3.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].proteinProgress))
                },
                color = proteinColor,
                strokeWidth = 3.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].fatProgress))
                },
                color = fatColor,
                strokeWidth = 3.dp.toPx()
            )
        }

        ChartDateLabels(points.first().date, points.last().date)

        val latestPoint = points.last()
        Text(
            text = "Latest: C ${latestPoint.carbsGram}/${latestPoint.targetCarbsGram}g · P ${latestPoint.proteinGram}/${latestPoint.targetProteinGram}g · F ${latestPoint.fatGram}/${latestPoint.targetFatGram}g",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF52624F)
        )
        ChartLegend(
            items = listOf(
                ChartLegendItem("Carbs", Color(0xFF3F7FC2)),
                ChartLegendItem("Protein", Color(0xFF2D6A4F)),
                ChartLegendItem("Fat", Color(0xFFD18B2F)),
                ChartLegendItem("Goal", Color(0xFF9AA39A))
            )
        )
    }
}

@Composable
private fun EmptyChartText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = Color(0xFF52624F)
    )
}

@Composable
private fun ChartDateLabels(
    startDate: LocalDate,
    endDate: LocalDate
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatMealDate(startDate),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF52624F)
        )
        Text(
            text = formatMealDate(endDate),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF52624F)
        )
    }
}

@Composable
private fun ChartLegend(items: List<ChartLegendItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowItems.forEach { item ->
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(item.color, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF52624F)
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun DrawScope.drawChartLine(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float
) {
    if (points.isEmpty()) return

    if (points.size > 1) {
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    points.forEach { point ->
        drawCircle(
            color = color,
            radius = strokeWidth * 0.9f,
            center = point
        )
    }
}

private data class CalorieChartPoint(
    val date: LocalDate,
    val calories: Int,
    val targetCalories: Int
)

private data class MacroChartPoint(
    val date: LocalDate,
    val carbsGram: Int,
    val targetCarbsGram: Int,
    val proteinGram: Int,
    val targetProteinGram: Int,
    val fatGram: Int,
    val targetFatGram: Int
) {
    val carbsProgress: Float = carbsGram / targetCarbsGram.toFloat()
    val proteinProgress: Float = proteinGram / targetProteinGram.toFloat()
    val fatProgress: Float = fatGram / targetFatGram.toFloat()
}

private data class ChartLegendItem(
    val label: String,
    val color: Color
)

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

private fun List<GoalPlanEntity>.goalFor(date: LocalDate): GoalPlanEntity? {
    val epochDay = date.toEpochDay()
    return firstOrNull { goalPlan ->
        goalPlan.validFromEpochDay <= epochDay && epochDay < goalPlan.validToEpochDay
    }
}
