package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppDanger
import com.jaewonlee.aidietrecord.ui.theme.AppDangerSoft
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppPrimary
import com.jaewonlee.aidietrecord.ui.theme.AppPrimarySoft
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.theme.AppSuccessSoft
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppSurfaceSoft
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.theme.AppTextPrimary
import com.jaewonlee.aidietrecord.ui.theme.AppWarningSoft
import com.jaewonlee.aidietrecord.ui.theme.MacroCarb
import com.jaewonlee.aidietrecord.ui.theme.MacroFat
import com.jaewonlee.aidietrecord.ui.theme.MacroProtein
import com.jaewonlee.aidietrecord.ui.util.formatMealDate
import com.jaewonlee.aidietrecord.ui.util.mealRecordDate
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.max

private const val CollapsedBodyMeasurementCount = 3

@Composable
fun RecentStatsScreen(
    mealRecords: List<MealRecord>,
    goalPlans: List<GoalPlanEntity>,
    bodyMeasurements: List<BodyMeasurementEntity>,
    targetCalories: Int,
    targetCarbsGram: Int,
    targetProteinGram: Int,
    targetFatGram: Int,
    onBackClick: () -> Unit
) {
    val dailyStats = remember(mealRecords) { mealRecords.toDailyStats() }
    val recentStats = dailyStats.take(7)
    val recentBodyMeasurements = remember(bodyMeasurements) {
        bodyMeasurements
            .sortedWith(
                compareByDescending<BodyMeasurementEntity> { it.measuredEpochDay }
                    .thenByDescending { it.createdAt }
            )
            .take(7)
    }
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
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
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
                        color = AppSuccess
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
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
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
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
                        fallbackTargetCarbsGram = targetCarbsGram,
                        fallbackTargetProteinGram = targetProteinGram,
                        fallbackTargetFatGram = targetFatGram
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Body Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BodyProgressPanel(measurements = recentBodyMeasurements)
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
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AppTextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChartSummaryChip(
    label: String,
    value: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
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
    val latestPoint = points.last()
    val calorieDelta = latestPoint.targetCalories - latestPoint.calories

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartSummaryChip(
                label = "Latest",
                value = "${latestPoint.calories} kcal",
                color = AppPrimary,
                containerColor = AppPrimarySoft,
                modifier = Modifier.weight(1f)
            )
            ChartSummaryChip(
                label = "Goal",
                value = "${latestPoint.targetCalories} kcal",
                color = AppTextMuted,
                containerColor = AppSurfaceSoft,
                modifier = Modifier.weight(1f)
            )
            ChartSummaryChip(
                label = if (calorieDelta < 0) "Over" else "Left",
                value = "${abs(calorieDelta)} kcal",
                color = if (calorieDelta < 0) AppDanger else AppSuccess,
                containerColor = if (calorieDelta < 0) AppDangerSoft else AppSuccessSoft,
                modifier = Modifier.weight(1f)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(152.dp)
        ) {
            val leftPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val gridColor = AppOutline.copy(alpha = 0.62f)
            val targetColor = AppTextMuted.copy(alpha = 0.72f)
            val calorieColor = AppPrimary

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
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].targetCalories))
                },
                color = targetColor,
                strokeWidth = 1.4.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].calories))
                },
                color = calorieColor,
                strokeWidth = 2.6.dp.toPx()
            )
        }

        ChartDateLabels(points.first().date, points.last().date)
        ChartLegend(
            items = listOf(
                ChartLegendItem("Intake", AppPrimary),
                ChartLegendItem("Goal", AppTextMuted)
            )
        )
    }
}

@Composable
private fun NutritionTrendChart(
    stats: List<DailyNutritionStats>,
    goalPlans: List<GoalPlanEntity>,
    fallbackTargetCarbsGram: Int,
    fallbackTargetProteinGram: Int,
    fallbackTargetFatGram: Int
) {
    if (stats.isEmpty()) {
        EmptyChartText("No nutrition records yet.")
        return
    }

    val points = stats.asReversed().map { dailyStats ->
        val goalPlan = goalPlans.goalFor(dailyStats.date)
        val targetCarbsGram = goalPlan?.dailyCarbsGram ?: fallbackTargetCarbsGram
        val targetProteinGram = goalPlan?.dailyProteinGram ?: fallbackTargetProteinGram
        val targetFatGram = goalPlan?.dailyFatGram ?: fallbackTargetFatGram

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
    val latestPoint = points.last()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartSummaryChip(
                label = "Carbs",
                value = "${latestPoint.carbsGram}g",
                color = MacroCarb,
                containerColor = AppSurfaceSoft,
                modifier = Modifier.weight(1f)
            )
            ChartSummaryChip(
                label = "Protein",
                value = "${latestPoint.proteinGram}g",
                color = MacroProtein,
                containerColor = AppSuccessSoft,
                modifier = Modifier.weight(1f)
            )
            ChartSummaryChip(
                label = "Fat",
                value = "${latestPoint.fatGram}g",
                color = MacroFat,
                containerColor = AppWarningSoft,
                modifier = Modifier.weight(1f)
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(152.dp)
        ) {
            val leftPadding = 8.dp.toPx()
            val rightPadding = 8.dp.toPx()
            val topPadding = 12.dp.toPx()
            val bottomPadding = 20.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding
            val gridColor = AppOutline.copy(alpha = 0.62f)
            val targetColor = AppTextMuted.copy(alpha = 0.72f)
            val carbColor = MacroCarb
            val proteinColor = MacroProtein
            val fatColor = MacroFat

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
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            val targetY = y(1f)
            drawLine(
                color = targetColor,
                start = Offset(leftPadding, targetY),
                end = Offset(leftPadding + chartWidth, targetY),
                strokeWidth = 1.4.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].carbsProgress))
                },
                color = carbColor,
                strokeWidth = 2.3.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].proteinProgress))
                },
                color = proteinColor,
                strokeWidth = 2.3.dp.toPx()
            )
            drawChartLine(
                points = points.indices.map { index ->
                    Offset(x(index), y(points[index].fatProgress))
                },
                color = fatColor,
                strokeWidth = 2.3.dp.toPx()
            )
        }

        ChartDateLabels(points.first().date, points.last().date)
        ChartLegend(
            items = listOf(
                ChartLegendItem("Carbs", MacroCarb),
                ChartLegendItem("Protein", MacroProtein),
                ChartLegendItem("Fat", MacroFat),
                ChartLegendItem("Goal", AppTextMuted)
            )
        )
    }
}

@Composable
private fun BodyProgressPanel(measurements: List<BodyMeasurementEntity>) {
    if (measurements.isEmpty()) {
        EmptyChartText("No body status records yet.")
        return
    }

    val chronologicalMeasurements = measurements.asReversed()
    val latestMeasurement = measurements.first()
    var showAllMeasurements by rememberSaveable(measurements.size) {
        mutableStateOf(false)
    }
    val displayedMeasurements = if (showAllMeasurements) {
        measurements
    } else {
        measurements.take(CollapsedBodyMeasurementCount)
    }
    val hiddenMeasurementCount = (measurements.size - CollapsedBodyMeasurementCount)
        .coerceAtLeast(0)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsChip(
                label = "Weight",
                value = latestMeasurement.weightKg.formatBodyMetric("kg"),
                modifier = Modifier.weight(1f)
            )
            StatsChip(
                label = "Muscle",
                value = latestMeasurement.muscleMassKg.formatBodyMetric("kg"),
                modifier = Modifier.weight(1f)
            )
            StatsChip(
                label = "Body Fat",
                value = latestMeasurement.bodyFatPercent.formatBodyMetric("%"),
                modifier = Modifier.weight(1f)
            )
        }

        BodyMeasurementTrendChart(measurements = chronologicalMeasurements)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayedMeasurements.forEach { measurement ->
                BodyMeasurementRow(measurement = measurement)
            }
            if (hiddenMeasurementCount > 0) {
                TextButton(
                    onClick = { showAllMeasurements = !showAllMeasurements },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (showAllMeasurements) {
                            "Show less"
                        } else {
                            "Show all ${measurements.size} records"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyMeasurementTrendChart(measurements: List<BodyMeasurementEntity>) {
    val points = measurements.map { measurement ->
        BodyChartPoint(
            date = LocalDate.ofEpochDay(measurement.measuredEpochDay),
            weightKg = measurement.weightKg,
            muscleMassKg = measurement.muscleMassKg,
            bodyFatPercent = measurement.bodyFatPercent
        )
    }
    val series = listOf(
        BodyChartSeries(
            label = "Weight",
            color = AppPrimary,
            values = points.mapIndexedNotNull { index, point ->
                point.weightKg?.let { BodyChartValue(index = index, value = it) }
            }
        ),
        BodyChartSeries(
            label = "Muscle",
            color = AppSuccess,
            values = points.mapIndexedNotNull { index, point ->
                point.muscleMassKg?.let { BodyChartValue(index = index, value = it) }
            }
        ),
        BodyChartSeries(
            label = "Body Fat",
            color = MacroFat,
            values = points.mapIndexedNotNull { index, point ->
                point.bodyFatPercent?.let { BodyChartValue(index = index, value = it) }
            }
        )
    ).filter { it.values.isNotEmpty() }

    if (series.isEmpty()) {
        EmptyChartText("Saved body records do not have chartable values yet.")
        return
    }

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
            val gridColor = AppOutline

            fun x(index: Int): Float {
                return if (points.size == 1) {
                    leftPadding + chartWidth / 2f
                } else {
                    leftPadding + chartWidth * (index / points.lastIndex.toFloat())
                }
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

            series.forEach { bodySeries ->
                val minValue = bodySeries.values.minOf { it.value }
                val maxValue = bodySeries.values.maxOf { it.value }
                val linePoints = bodySeries.values.map { bodyValue ->
                    val normalizedValue = normalizeBodyChartValue(
                        value = bodyValue.value,
                        minValue = minValue,
                        maxValue = maxValue
                    )
                    Offset(
                        x = x(bodyValue.index),
                        y = topPadding + chartHeight * (1f - normalizedValue)
                    )
                }
                drawChartLine(
                    points = linePoints,
                    color = bodySeries.color,
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        ChartDateLabels(points.first().date, points.last().date)
        ChartLegend(
            items = series.map { ChartLegendItem(it.label, it.color) }
        )
    }
}

@Composable
private fun BodyMeasurementRow(measurement: BodyMeasurementEntity) {
    val measuredDate = LocalDate.ofEpochDay(measurement.measuredEpochDay)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = formatMealDate(measuredDate),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppTextPrimary
        )
        Text(
            text = measurement.bodyMeasurementValuesText(),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTextMuted
        )
    }
}

@Composable
private fun EmptyChartText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTextMuted
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
            color = AppTextMuted
        )
        Text(
            text = formatMealDate(endDate),
            style = MaterialTheme.typography.labelMedium,
            color = AppTextMuted
        )
    }
}

@Composable
private fun ChartLegend(items: List<ChartLegendItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .background(AppSurfaceSoft, RoundedCornerShape(50))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(item.color, RoundedCornerShape(50))
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTextMuted
                )
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
            radius = strokeWidth * 0.72f,
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

private data class BodyChartPoint(
    val date: LocalDate,
    val weightKg: Double?,
    val muscleMassKg: Double?,
    val bodyFatPercent: Double?
)

private data class BodyChartSeries(
    val label: String,
    val color: Color,
    val values: List<BodyChartValue>
)

private data class BodyChartValue(
    val index: Int,
    val value: Double
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

private fun normalizeBodyChartValue(
    value: Double,
    minValue: Double,
    maxValue: Double
): Float {
    if (abs(maxValue - minValue) < 0.0001) {
        return 0.5f
    }
    return ((value - minValue) / (maxValue - minValue)).toFloat().coerceIn(0f, 1f)
}

private fun BodyMeasurementEntity.bodyMeasurementValuesText(): String {
    val values = buildList {
        weightKg?.let { add("Weight ${it.formatBodyMetric("kg")}") }
        muscleMassKg?.let { add("Muscle ${it.formatBodyMetric("kg")}") }
        basalMetabolicRateKcal?.let { add("BMR ${it}kcal") }
        bodyFatMassKg?.let { add("Fat mass ${it.formatBodyMetric("kg")}") }
        bodyFatPercent?.let { add("Body fat ${it.formatBodyMetric("%")}") }
    }
    return values.joinToString(" · ").ifBlank { "No values saved" }
}

private fun Double?.formatBodyMetric(unit: String): String {
    return this?.let { value ->
        val numberText = if (abs(value % 1.0) < 0.0001) {
            value.toInt().toString()
        } else {
            "%.1f".format(value)
        }
        "$numberText$unit"
    } ?: "-"
}

private fun List<GoalPlanEntity>.goalFor(date: LocalDate): GoalPlanEntity? {
    val epochDay = date.toEpochDay()
    return firstOrNull { goalPlan ->
        goalPlan.validFromEpochDay <= epochDay && epochDay < goalPlan.validToEpochDay
    }
}
