package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.ui.theme.AppDanger
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppBackground
import com.jaewonlee.aidietrecord.ui.theme.AppDangerSoft
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppPrimary
import com.jaewonlee.aidietrecord.ui.theme.AppPrimarySoft
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.theme.AppSuccessSoft
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppSurfaceSoft
import com.jaewonlee.aidietrecord.ui.theme.AppSurfaceTonal
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.theme.AppTextPrimary
import com.jaewonlee.aidietrecord.ui.theme.AppWarning
import com.jaewonlee.aidietrecord.ui.theme.MacroCarb
import com.jaewonlee.aidietrecord.ui.theme.MacroFat
import com.jaewonlee.aidietrecord.ui.theme.MacroFiber
import com.jaewonlee.aidietrecord.ui.theme.MacroProtein
import com.jaewonlee.aidietrecord.ui.theme.MacroSodium
import com.jaewonlee.aidietrecord.ui.theme.MacroSugar
import com.jaewonlee.aidietrecord.ui.util.isTodayMeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HomeBackground = AppBackground
private val CardSurface = AppSurface
private val TonalSurface = AppSurfaceTonal
private val TrackColor = AppOutline
private val TextPrimary = AppTextPrimary
private val TextMuted = AppTextMuted
private val PrimaryAction = AppPrimary
private val CarbColor = MacroCarb
private val ProteinColor = MacroProtein
private val FatColor = MacroFat
private val FiberColor = MacroFiber
private val SugarColor = MacroSugar
private val SodiumColor = MacroSodium

data class MealAnalysisNoticeUiState(
    val isAnalyzing: Boolean,
    val reviewedMeal: MealRecord?,
    val errorMessage: String?
)

@Composable
fun HomeScreen(
    nickname: String,
    mealRecords: List<MealRecord>,
    mealAnalysisNotice: MealAnalysisNoticeUiState?,
    targetCalories: Int,
    targetCarbsGram: Int,
    targetProteinGram: Int,
    targetFatGram: Int,
    targetFiberGram: Int,
    targetSugarGram: Int,
    targetSodiumMilligram: Int,
    onAddMealClick: () -> Unit,
    onMealListClick: () -> Unit,
    onGoalSettingsClick: () -> Unit,
    onRecentStatsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onReviewMealClick: () -> Unit,
    onRetryMealAnalysisClick: () -> Unit,
    onDismissMealAnalysisClick: () -> Unit
) {
    val todayMeals = mealRecords.filter { isTodayMeal(it.createdAt) }
    val totalCalories = todayMeals.sumOf { it.calories }
    val totalCarbs = todayMeals.sumOf { it.carbsGram }
    val totalProtein = todayMeals.sumOf { it.proteinGram }
    val totalFat = todayMeals.sumOf { it.fatGram }
    val totalFiber = todayMeals.sumOf { it.fiberGram }
    val totalSugar = todayMeals.sumOf { it.sugarGram }
    val totalSodium = todayMeals.sumOf { it.sodiumMilligram }
    val dailyCalories = targetCalories.coerceAtLeast(1)
    val calorieDelta = dailyCalories - totalCalories
    val calorieProgress = (totalCalories / dailyCalories.toFloat()).coerceIn(0f, 1f)
    val todayLabel = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH))

    Scaffold(
        containerColor = HomeBackground,
        bottomBar = {
            HomeBottomBar(
                onRecentStatsClick = onRecentStatsClick,
                onAddMealClick = onAddMealClick,
                onGoalSettingsClick = onGoalSettingsClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HomeHeader(
                nickname = nickname,
                todayLabel = todayLabel,
                onProfileClick = onProfileClick
            )

            mealAnalysisNotice?.let { notice ->
                MealAnalysisNoticeCard(
                    notice = notice,
                    onReviewMealClick = onReviewMealClick,
                    onRetryMealAnalysisClick = onRetryMealAnalysisClick,
                    onDismissMealAnalysisClick = onDismissMealAnalysisClick
                )
            }

            CalorieHero(
                totalCalories = totalCalories,
                targetCalories = dailyCalories,
                calorieDelta = calorieDelta,
                progress = calorieProgress,
                onMealListClick = onMealListClick
            )

            NutritionPanel(
                carbsGram = totalCarbs,
                proteinGram = totalProtein,
                fatGram = totalFat,
                fiberGram = totalFiber,
                sugarGram = totalSugar,
                sodiumMilligram = totalSodium,
                targetCarbsGram = targetCarbsGram.coerceAtLeast(1),
                targetProteinGram = targetProteinGram.coerceAtLeast(1),
                targetFatGram = targetFatGram.coerceAtLeast(1),
                targetFiberGram = targetFiberGram.coerceAtLeast(1),
                targetSugarGram = targetSugarGram.coerceAtLeast(1),
                targetSodiumMilligram = targetSodiumMilligram.coerceAtLeast(1)
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MealAnalysisNoticeCard(
    notice: MealAnalysisNoticeUiState,
    onReviewMealClick: () -> Unit,
    onRetryMealAnalysisClick: () -> Unit,
    onDismissMealAnalysisClick: () -> Unit
) {
    val noticeContainerColor = when {
        notice.isAnalyzing -> AppPrimarySoft
        notice.errorMessage != null -> AppDangerSoft
        else -> AppSuccessSoft
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = noticeContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppOutline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                notice.isAnalyzing -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "AI is analyzing your meal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "It will appear here for review before it affects today's totals.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
                notice.errorMessage != null -> {
                    Text(
                        text = "Meal analysis failed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = notice.errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismissMealAnalysisClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = onRetryMealAnalysisClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                notice.reviewedMeal != null -> {
                    val reviewedMeal = notice.reviewedMeal
                    Text(
                        text = "Meal analysis ready",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${reviewedMeal.foods.size} food item(s) detected · ${reviewedMeal.calories} kcal estimated",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Button(
                        onClick = onReviewMealClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Review Result")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    nickname: String,
    todayLabel: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "AI Meal Log",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = todayLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
        ProfileActionButton(
            label = profileInitial(nickname),
            onClick = onProfileClick
        )
    }
}

@Composable
private fun CalorieHero(
    totalCalories: Int,
    targetCalories: Int,
    calorieDelta: Int,
    progress: Float,
    onMealListClick: () -> Unit
) {
    val isOverGoal = calorieDelta < 0
    val statusText = if (isOverGoal) {
        "Over goal by ${-calorieDelta} kcal"
    } else {
        "$calorieDelta kcal left"
    }
    val statusColor = if (isOverGoal) AppDanger else AppSuccess

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Intake",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = onMealListClick) {
                Text(
                    text = "History",
                    color = PrimaryAction,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CalorieGauge(
                totalCalories = totalCalories,
                targetCalories = targetCalories,
                progress = progress,
                overRatio = ((totalCalories - targetCalories).coerceAtLeast(0) / targetCalories.toFloat())
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "$targetCalories kcal daily goal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                StatusPill(
                    text = if (isOverGoal) "Over target" else "On pace",
                    color = statusColor,
                    containerColor = if (isOverGoal) AppDangerSoft else AppSuccessSoft
                )
            }
        }
    }
}

@Composable
private fun CalorieGauge(
    totalCalories: Int,
    targetCalories: Int,
    progress: Float,
    overRatio: Float
) {
    Box(
        modifier = Modifier.size(168.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 13.dp.toPx()
            val arcSize = size.minDimension - strokeWidth
            val topLeft = Offset(
                x = (size.width - arcSize) / 2f,
                y = (size.height - arcSize) / 2f
            )
            val arcStroke = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            )

            drawArc(
                color = TrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = arcStroke
            )
            drawArc(
                color = PrimaryAction,
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = arcStroke
            )
            if (overRatio > 0f) {
                drawArc(
                    color = AppDanger,
                    startAngle = -90f,
                    sweepAngle = 360f * overRatio.coerceIn(0.04f, 0.25f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = totalCalories.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "of $targetCalories kcal",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NutritionPanel(
    carbsGram: Int,
    proteinGram: Int,
    fatGram: Int,
    fiberGram: Int,
    sugarGram: Int,
    sodiumMilligram: Int,
    targetCarbsGram: Int,
    targetProteinGram: Int,
    targetFatGram: Int,
    targetFiberGram: Int,
    targetSugarGram: Int,
    targetSodiumMilligram: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nutrition",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroRingChip(
                label = "Carbs",
                value = carbsGram,
                target = targetCarbsGram,
                unit = "g",
                color = CarbColor,
                modifier = Modifier.weight(1f)
            )
            MacroRingChip(
                label = "Protein",
                value = proteinGram,
                target = targetProteinGram,
                unit = "g",
                color = ProteinColor,
                modifier = Modifier.weight(1f)
            )
            MacroRingChip(
                label = "Fat",
                value = fatGram,
                target = targetFatGram,
                unit = "g",
                color = FatColor,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = AppOutline)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HealthStatusCard(
                label = "Fiber",
                value = fiberGram,
                target = targetFiberGram,
                unit = "g",
                status = if (fiberGram >= targetFiberGram) "Good" else "Low",
                color = FiberColor,
                statusColor = if (fiberGram >= targetFiberGram) AppSuccess else AppWarning,
                modifier = Modifier.weight(1f)
            )
            HealthStatusCard(
                label = "Sugar",
                value = sugarGram,
                target = targetSugarGram,
                unit = "g",
                status = if (sugarGram > targetSugarGram) "High" else "OK",
                color = SugarColor,
                statusColor = if (sugarGram > targetSugarGram) AppDanger else AppSuccess,
                modifier = Modifier.weight(1f)
            )
            HealthStatusCard(
                label = "Sodium",
                value = sodiumMilligram,
                target = targetSodiumMilligram,
                unit = "mg",
                status = if (sodiumMilligram > targetSodiumMilligram) "High" else "OK",
                color = SodiumColor,
                statusColor = if (sodiumMilligram > targetSodiumMilligram) AppWarning else AppSuccess,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MacroRingChip(
    label: String,
    value: Int,
    target: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = (value / target.toFloat()).coerceIn(0f, 1f)
    val overAmount = (value - target).coerceAtLeast(0)

    Column(
        modifier = modifier
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .border(1.dp, AppOutline, RoundedCornerShape(8.dp))
            .heightIn(min = 112.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniProgressRing(progress = progress, color = color)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
                Text(
                    text = "$value$unit",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
        if (overAmount > 0) {
            StatusPill(
                text = "+$overAmount$unit",
                color = AppDanger,
                containerColor = AppDangerSoft
            )
        } else {
            Text(
                text = "Goal $target$unit",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun MiniProgressRing(
    progress: Float,
    color: Color
) {
    Canvas(modifier = Modifier.size(34.dp)) {
        val strokeWidth = 4.dp.toPx()
        val arcSize = size.minDimension - strokeWidth
        val topLeft = Offset(
            x = (size.width - arcSize) / 2f,
            y = (size.height - arcSize) / 2f
        )
        drawArc(
            color = TrackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun HealthStatusCard(
    label: String,
    value: Int,
    target: Int,
    unit: String,
    status: String,
    color: Color,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .border(1.dp, AppOutline, RoundedCornerShape(8.dp))
            .heightIn(min = 112.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted
            )
        }
        Text(
            text = "$value$unit",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "$status · $target$unit",
            style = MaterialTheme.typography.labelSmall,
            color = statusColor
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun HomeBottomBar(
    onRecentStatsClick: () -> Unit,
    onAddMealClick: () -> Unit,
    onGoalSettingsClick: () -> Unit
) {
    Surface(
        color = CardSurface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTextButton(
                text = "Insights",
                onClick = onRecentStatsClick,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onAddMealClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAction),
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "+ Add",
                    fontWeight = FontWeight.Bold
                )
            }
            BottomNavTextButton(
                text = "Goals",
                onClick = onGoalSettingsClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomNavTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = PrimaryAction,
            fontWeight = FontWeight.SemiBold
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
            .size(44.dp)
            .background(TonalSurface, CircleShape)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ProteinColor
        )
    }
}

private fun profileInitial(nickname: String): String {
    return nickname.trim().firstOrNull()?.toString() ?: "J"
}
