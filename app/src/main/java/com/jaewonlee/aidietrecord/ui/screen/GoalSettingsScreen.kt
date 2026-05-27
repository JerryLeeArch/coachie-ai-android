package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementDraft
import com.jaewonlee.aidietrecord.data.model.BodyMeasurementEntity
import com.jaewonlee.aidietrecord.data.model.GoalPlanDraft
import com.jaewonlee.aidietrecord.data.model.GoalPlanEntity
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppPrimary
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppSurfaceSoft
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.theme.AppWarning
import com.jaewonlee.aidietrecord.ui.theme.MacroCarb
import com.jaewonlee.aidietrecord.ui.theme.MacroFat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val PERIOD_MODE_TARGET_DATE = "TARGET_DATE"
private const val PERIOD_MODE_DURATION = "DURATION"
private val GoalProgressColor = AppPrimary
private val WeightProgressColor = AppSuccess
private val MuscleProgressColor = MacroCarb
private val BodyFatProgressColor = MacroFat
private val GoalTrackColor = AppOutline

@Composable
fun GoalSettingsScreen(
    goalStartDate: String,
    onGoalStartDateChange: (String) -> Unit,
    goalEndDate: String,
    onGoalEndDateChange: (String) -> Unit,
    goalPeriodMode: String,
    onGoalPeriodModeChange: (String) -> Unit,
    currentWeight: String,
    onCurrentWeightChange: (String) -> Unit,
    currentMuscleMass: String,
    onCurrentMuscleMassChange: (String) -> Unit,
    currentMetabolicRate: String,
    onCurrentMetabolicRateChange: (String) -> Unit,
    currentBodyFatPercent: String,
    onCurrentBodyFatPercentChange: (String) -> Unit,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    targetMuscleMass: String,
    onTargetMuscleMassChange: (String) -> Unit,
    targetBodyFatPercent: String,
    onTargetBodyFatPercentChange: (String) -> Unit,
    targetWeeks: String,
    onTargetWeeksChange: (String) -> Unit,
    targetCalories: String,
    onTargetCaloriesChange: (String) -> Unit,
    targetCarbsGram: String,
    onTargetCarbsGramChange: (String) -> Unit,
    proteinGoal: String,
    onProteinGoalChange: (String) -> Unit,
    targetFatGram: String,
    onTargetFatGramChange: (String) -> Unit,
    targetFiberGram: String,
    onTargetFiberGramChange: (String) -> Unit,
    targetSugarGram: String,
    onTargetSugarGramChange: (String) -> Unit,
    targetSodiumMilligram: String,
    onTargetSodiumMilligramChange: (String) -> Unit,
    manualTargetsEnabled: Boolean,
    onManualTargetsEnabledChange: (Boolean) -> Unit,
    currentGoalPlan: GoalPlanEntity?,
    latestBodyMeasurement: BodyMeasurementEntity?,
    onSaveBodyMeasurement: (BodyMeasurementDraft) -> Unit,
    onSaveClick: (GoalPlanDraft) -> Unit,
    onBackClick: () -> Unit
) {
    var isEditingGoal by rememberSaveable(currentGoalPlan?.id) {
        mutableStateOf(currentGoalPlan == null)
    }
    var showCurrentWeightError by rememberSaveable { mutableStateOf(false) }
    var showDateRangeError by rememberSaveable { mutableStateOf(false) }
    var showBodyMeasurementForm by rememberSaveable { mutableStateOf(false) }
    var bodyMeasurementDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var bodyMeasurementWeight by rememberSaveable { mutableStateOf("") }
    var bodyMeasurementMetabolicRate by rememberSaveable { mutableStateOf("") }
    var bodyMeasurementMuscleMass by rememberSaveable { mutableStateOf("") }
    var bodyMeasurementFatMass by rememberSaveable { mutableStateOf("") }
    var bodyMeasurementFatPercent by rememberSaveable { mutableStateOf("") }
    var bodyMeasurementError by rememberSaveable { mutableStateOf<String?>(null) }
    var bodySuggestionNotice by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingGoalProposal by remember { mutableStateOf<GoalPlanProposal?>(null) }
    val currentWeightIsInvalid = showCurrentWeightError && !currentWeight.hasPositiveNumber()
    val computedGoalEndDate = buildTargetDateFromDuration(goalStartDate, targetWeeks)
    val effectiveGoalEndDate = if (goalPeriodMode == PERIOD_MODE_DURATION) {
        computedGoalEndDate.orEmpty()
    } else {
        goalEndDate
    }
    val dateRange = parseGoalDateRange(goalStartDate, effectiveGoalEndDate)
    val dateRangeIsInvalid = showDateRangeError && dateRange == null
    val effectiveTargetWeeks = dateRange?.durationWeeks
        ?: targetWeeks.toIntOrNull()?.takeIf { it > 0 }
        ?: 8
    val nutritionPlan = buildNutritionPlan(
        currentWeight = currentWeight,
        currentMuscleMass = currentMuscleMass,
        currentMetabolicRate = currentMetabolicRate,
        currentBodyFatPercent = currentBodyFatPercent,
        targetWeight = targetWeight,
        targetMuscleMass = targetMuscleMass,
        targetBodyFatPercent = targetBodyFatPercent,
        targetWeeks = effectiveTargetWeeks.toString()
    )
    val applyBodyMeasurementToGoalDraft: (BodyMeasurementDraft) -> Unit = { draft ->
        val knownWeight = draft.weightKg ?: currentWeight.toPositiveDoubleOrNull()
        val bodyFatPercent = draft.bodyFatPercent ?: bodyFatPercentFromMass(
            bodyFatMassKg = draft.bodyFatMassKg,
            weightKg = knownWeight
        )

        draft.weightKg?.let { value ->
            onCurrentWeightChange(value.toGoalInputText())
            showCurrentWeightError = false
        }
        draft.muscleMassKg?.let { value ->
            onCurrentMuscleMassChange(value.toGoalInputText())
        }
        draft.basalMetabolicRateKcal?.let { value ->
            onCurrentMetabolicRateChange(value.toString())
        }
        bodyFatPercent?.let { value ->
            onCurrentBodyFatPercentChange(value.toGoalInputText())
        }
        draft.muscleMassKg
            ?.takeIf { targetMuscleMass.isBlank() }
            ?.let { value -> onTargetMuscleMassChange(value.toGoalInputText()) }
        bodyFatPercent
            ?.let(::suggestTargetBodyFatPercent)
            ?.takeIf { targetBodyFatPercent.isBlank() }
            ?.let { value -> onTargetBodyFatPercentChange(value.toGoalInputText()) }
        suggestTargetWeight(
            currentWeightKg = knownWeight,
            currentBodyFatPercent = bodyFatPercent,
            targetBodyFatPercent = bodyFatPercent?.let(::suggestTargetBodyFatPercent)
        )
            ?.takeIf { targetWeight.isBlank() }
            ?.let { value -> onTargetWeightChange(value.toGoalInputText()) }
        bodySuggestionNotice = "AI suggestion applied to the goal draft. Generate a proposal, then confirm it to save."
        onManualTargetsEnabledChange(false)
    }
    val saveBodyMeasurementInput: (Boolean) -> Unit = { applyToGoalDraft ->
        val measuredEpochDay = parseMeasurementEpochDay(bodyMeasurementDate)
        if (measuredEpochDay == null) {
            bodyMeasurementError = "Use YYYY-MM-DD for the measured date."
        } else {
            val weight = bodyMeasurementWeight.toPositiveDoubleOrNull()
            val muscleMass = bodyMeasurementMuscleMass.toPositiveDoubleOrNull()
            val metabolicRate = bodyMeasurementMetabolicRate.toPositiveIntOrNull()
            val bodyFatMass = bodyMeasurementFatMass.toPositiveDoubleOrNull()
            val explicitBodyFatPercent = bodyMeasurementFatPercent.toNonNegativeDoubleOrNull()
            val bodyFatPercent = explicitBodyFatPercent ?: bodyFatPercentFromMass(
                bodyFatMassKg = bodyFatMass,
                weightKg = weight ?: currentWeight.toPositiveDoubleOrNull() ?: currentGoalPlan?.startWeightKg
            )
            val draft = BodyMeasurementDraft(
                measuredEpochDay = measuredEpochDay,
                weightKg = weight,
                muscleMassKg = muscleMass,
                basalMetabolicRateKcal = metabolicRate,
                bodyFatMassKg = bodyFatMass,
                bodyFatPercent = bodyFatPercent
            )
            if (!draft.hasAnyValue()) {
                bodyMeasurementError = "Enter at least one body value."
            } else {
                onSaveBodyMeasurement(draft)
                if (applyToGoalDraft) {
                    applyBodyMeasurementToGoalDraft(draft)
                }
                bodyMeasurementWeight = ""
                bodyMeasurementMetabolicRate = ""
                bodyMeasurementMuscleMass = ""
                bodyMeasurementFatMass = ""
                bodyMeasurementFatPercent = ""
                bodyMeasurementError = null
                showBodyMeasurementForm = false
                pendingGoalProposal = null
            }
        }
    }

    ScreenScaffold(
        title = if (currentGoalPlan != null && !isEditingGoal) "Goals" else "Goal Settings",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentGoalPlan != null && !isEditingGoal) {
                GoalProgressOverview(
                    goalPlan = currentGoalPlan,
                    latestBodyMeasurement = latestBodyMeasurement
                )
                OutlinedButton(
                    onClick = {
                        showBodyMeasurementForm = !showBodyMeasurementForm
                        bodyMeasurementDate = LocalDate.now().toString()
                        bodyMeasurementError = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showBodyMeasurementForm) "Hide Body Status Input" else "Input Current Body Status")
                }
                if (showBodyMeasurementForm) {
                    GoalCard(title = "Current Body Status") {
                        BodyMeasurementForm(
                            measuredDate = bodyMeasurementDate,
                            onMeasuredDateChange = {
                                bodyMeasurementDate = it
                                bodyMeasurementError = null
                            },
                            weight = bodyMeasurementWeight,
                            onWeightChange = {
                                bodyMeasurementWeight = it
                                bodyMeasurementError = null
                            },
                            metabolicRate = bodyMeasurementMetabolicRate,
                            onMetabolicRateChange = {
                                bodyMeasurementMetabolicRate = it
                                bodyMeasurementError = null
                            },
                            muscleMass = bodyMeasurementMuscleMass,
                            onMuscleMassChange = {
                                bodyMeasurementMuscleMass = it
                                bodyMeasurementError = null
                            },
                            bodyFatMass = bodyMeasurementFatMass,
                            onBodyFatMassChange = {
                                bodyMeasurementFatMass = it
                                bodyMeasurementError = null
                            },
                            bodyFatPercent = bodyMeasurementFatPercent,
                            onBodyFatPercentChange = {
                                bodyMeasurementFatPercent = it
                                bodyMeasurementError = null
                            },
                            errorMessage = bodyMeasurementError,
                            onSaveClick = { saveBodyMeasurementInput(false) },
                            onCancelClick = {
                                showBodyMeasurementForm = false
                                bodyMeasurementError = null
                            }
                        )
                    }
                }
                Button(
                    onClick = { isEditingGoal = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Goal")
                }
            } else {
            Text(
                text = "Set a required body weight, optional body composition, and target values. The app will build a daily nutrition plan from them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            GoalCard(title = "Goal Period") {
                GoalFieldRow {
                    PeriodModeButton(
                        text = "Target Date",
                        selected = goalPeriodMode == PERIOD_MODE_TARGET_DATE,
                        onClick = { onGoalPeriodModeChange(PERIOD_MODE_TARGET_DATE) },
                        modifier = Modifier.weight(1f)
                    )
                    PeriodModeButton(
                        text = "Duration",
                        selected = goalPeriodMode == PERIOD_MODE_DURATION,
                        onClick = { onGoalPeriodModeChange(PERIOD_MODE_DURATION) },
                        modifier = Modifier.weight(1f)
                    )
                }
                GoalFieldRow {
                    GoalDateField(
                        value = goalStartDate,
                        onValueChange = {
                            onGoalStartDateChange(it)
                            showDateRangeError = false
                        },
                        label = "Start Date",
                        isError = dateRangeIsInvalid,
                        modifier = Modifier.weight(1f)
                    )
                    if (goalPeriodMode == PERIOD_MODE_TARGET_DATE) {
                        GoalDateField(
                            value = goalEndDate,
                            onValueChange = {
                                onGoalEndDateChange(it)
                                showDateRangeError = false
                            },
                            label = "Target Date",
                            isError = dateRangeIsInvalid,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        GoalNumberField(
                            value = targetWeeks,
                            onValueChange = {
                                onTargetWeeksChange(it.filter(Char::isDigit))
                                showDateRangeError = false
                            },
                            label = "Duration",
                            suffix = "weeks",
                            allowDecimal = false,
                            isRequiredError = dateRangeIsInvalid,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text(
                    text = dateRange?.let {
                        "Plan period: ${it.startDateText} ~ ${it.endDateText} · ${it.durationDays} days (${it.durationWeeks} weeks)"
                    } ?: "Use YYYY-MM-DD and make the target date later than the start date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (dateRangeIsInvalid) MaterialTheme.colorScheme.error else AppSuccess
                )
            }

            GoalCard(title = "Current Profile") {
                if (latestBodyMeasurement != null) {
                    Text(
                        text = latestBodyMeasurement.summaryText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextMuted
                    )
                    OutlinedButton(
                        onClick = {
                            applyBodyMeasurementToGoalDraft(latestBodyMeasurement.toDraft())
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use Latest Body Status")
                    }
                }
                OutlinedButton(
                    onClick = {
                        showBodyMeasurementForm = !showBodyMeasurementForm
                        bodyMeasurementDate = LocalDate.now().toString()
                        bodyMeasurementError = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showBodyMeasurementForm) "Hide Body Status Input" else "Input Current Body Status")
                }
                if (showBodyMeasurementForm) {
                    BodyMeasurementForm(
                        measuredDate = bodyMeasurementDate,
                        onMeasuredDateChange = {
                            bodyMeasurementDate = it
                            bodyMeasurementError = null
                        },
                        weight = bodyMeasurementWeight,
                        onWeightChange = {
                            bodyMeasurementWeight = it
                            bodyMeasurementError = null
                        },
                        metabolicRate = bodyMeasurementMetabolicRate,
                        onMetabolicRateChange = {
                            bodyMeasurementMetabolicRate = it
                            bodyMeasurementError = null
                        },
                        muscleMass = bodyMeasurementMuscleMass,
                        onMuscleMassChange = {
                            bodyMeasurementMuscleMass = it
                            bodyMeasurementError = null
                        },
                        bodyFatMass = bodyMeasurementFatMass,
                        onBodyFatMassChange = {
                            bodyMeasurementFatMass = it
                            bodyMeasurementError = null
                        },
                        bodyFatPercent = bodyMeasurementFatPercent,
                        onBodyFatPercentChange = {
                            bodyMeasurementFatPercent = it
                            bodyMeasurementError = null
                        },
                        errorMessage = bodyMeasurementError,
                        onSaveClick = { saveBodyMeasurementInput(true) },
                        onCancelClick = {
                            showBodyMeasurementForm = false
                            bodyMeasurementError = null
                        }
                    )
                }
                bodySuggestionNotice?.let { notice ->
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppSuccess
                    )
                }
                GoalFieldRow {
                    GoalNumberField(
                        value = currentWeight,
                        onValueChange = {
                            val filteredValue = filterDecimalInput(it)
                            onCurrentWeightChange(filteredValue)
                            if (filteredValue.hasPositiveNumber()) {
                                showCurrentWeightError = false
                            }
                        },
                        label = "Current Weight *",
                        suffix = "kg",
                        isRequiredError = currentWeightIsInvalid,
                        modifier = Modifier.weight(1f)
                    )
                    GoalNumberField(
                        value = currentMuscleMass,
                        onValueChange = { onCurrentMuscleMassChange(filterDecimalInput(it)) },
                        label = "Skeletal Muscle",
                        suffix = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }
                GoalFieldRow {
                    GoalNumberField(
                        value = currentBodyFatPercent,
                        onValueChange = { onCurrentBodyFatPercentChange(filterDecimalInput(it)) },
                        label = "Body Fat %",
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                    GoalNumberField(
                        value = currentMetabolicRate,
                        onValueChange = { onCurrentMetabolicRateChange(it.filter(Char::isDigit)) },
                        label = "Metabolic Rate",
                        suffix = "kcal",
                        allowDecimal = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            GoalCard(title = "Target Profile") {
                GoalFieldRow {
                    GoalNumberField(
                        value = targetWeight,
                        onValueChange = { onTargetWeightChange(filterDecimalInput(it)) },
                        label = "Target Weight",
                        suffix = "kg",
                        modifier = Modifier.weight(1f)
                    )
                    GoalNumberField(
                        value = targetMuscleMass,
                        onValueChange = { onTargetMuscleMassChange(filterDecimalInput(it)) },
                        label = "Target Muscle",
                        suffix = "kg",
                        modifier = Modifier.weight(1f)
                    )
                }
                GoalFieldRow {
                    GoalNumberField(
                        value = targetBodyFatPercent,
                        onValueChange = { onTargetBodyFatPercentChange(filterDecimalInput(it)) },
                        label = "Target Fat %",
                        suffix = "%",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = buildGoalSummary(currentWeight, targetWeight, effectiveTargetWeeks.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppSuccess
                )
            }

            GoalCard(title = "AI Nutrition Plan") {
                if (nutritionPlan == null) {
                    Text(
                        text = "Enter current weight to generate a daily plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = nutritionPlan.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextMuted
                    )
                    NutritionPlanRow("Calories", "${nutritionPlan.calories} kcal")
                    NutritionPlanRow(
                        "Macros",
                        "Carbs ${nutritionPlan.carbsGram}g · Protein ${nutritionPlan.proteinGram}g · Fat ${nutritionPlan.fatGram}g"
                    )
                    NutritionPlanRow(
                        "Health limits",
                        "Fiber ${nutritionPlan.fiberGram}g · Sugar ${nutritionPlan.sugarGram}g · Sodium ${nutritionPlan.sodiumMilligram}mg"
                    )
                }
            }

            GoalCard(title = "Daily Targets") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (manualTargetsEnabled) "Manual Override" else "AI Plan",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = manualTargetsEnabled,
                        onCheckedChange = { enabled ->
                            onManualTargetsEnabledChange(enabled)
                            if (enabled && nutritionPlan != null) {
                                onTargetCaloriesChange(nutritionPlan.calories.toString())
                                onTargetCarbsGramChange(nutritionPlan.carbsGram.toString())
                                onProteinGoalChange(nutritionPlan.proteinGram.toString())
                                onTargetFatGramChange(nutritionPlan.fatGram.toString())
                                onTargetFiberGramChange(nutritionPlan.fiberGram.toString())
                                onTargetSugarGramChange(nutritionPlan.sugarGram.toString())
                                onTargetSodiumMilligramChange(nutritionPlan.sodiumMilligram.toString())
                            }
                        }
                    )
                }
                if (manualTargetsEnabled) {
                    GoalFieldRow {
                        GoalNumberField(
                            value = targetCalories,
                            onValueChange = { onTargetCaloriesChange(it.filter(Char::isDigit)) },
                            label = "Calories",
                            suffix = "kcal",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                        GoalNumberField(
                            value = targetCarbsGram,
                            onValueChange = { onTargetCarbsGramChange(it.filter(Char::isDigit)) },
                            label = "Carbs",
                            suffix = "g",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    GoalFieldRow {
                        GoalNumberField(
                            value = proteinGoal,
                            onValueChange = { onProteinGoalChange(it.filter(Char::isDigit)) },
                            label = "Protein",
                            suffix = "g",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                        GoalNumberField(
                            value = targetFatGram,
                            onValueChange = { onTargetFatGramChange(it.filter(Char::isDigit)) },
                            label = "Fat",
                            suffix = "g",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    GoalFieldRow {
                        GoalNumberField(
                            value = targetFiberGram,
                            onValueChange = { onTargetFiberGramChange(it.filter(Char::isDigit)) },
                            label = "Fiber",
                            suffix = "g",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                        GoalNumberField(
                            value = targetSugarGram,
                            onValueChange = { onTargetSugarGramChange(it.filter(Char::isDigit)) },
                            label = "Sugar",
                            suffix = "g",
                            allowDecimal = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    GoalNumberField(
                        value = targetSodiumMilligram,
                        onValueChange = { onTargetSodiumMilligramChange(it.filter(Char::isDigit)) },
                        label = "Sodium",
                        suffix = "mg",
                        allowDecimal = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            pendingGoalProposal?.let { proposal ->
                GoalProposalCard(
                    proposal = proposal,
                    onEditDetailsClick = { pendingGoalProposal = null },
                    onConfirmClick = { onSaveClick(proposal.draft) }
                )
            } ?: Button(
                onClick = {
                    val parsedGoalEndDate = if (goalPeriodMode == PERIOD_MODE_DURATION) {
                        buildTargetDateFromDuration(goalStartDate, targetWeeks).orEmpty()
                    } else {
                        goalEndDate
                    }
                    val parsedDateRange = parseGoalDateRange(goalStartDate, parsedGoalEndDate)
                    val parsedCurrentWeight = currentWeight.toPositiveDoubleOrNull()
                    showDateRangeError = parsedDateRange == null
                    showCurrentWeightError = parsedCurrentWeight == null
                    if (parsedDateRange != null && parsedCurrentWeight != null) {
                        pendingGoalProposal = buildGoalProposal(
                            dateRange = parsedDateRange,
                            currentWeight = currentWeight,
                            currentMuscleMass = currentMuscleMass,
                            currentMetabolicRate = currentMetabolicRate,
                            currentBodyFatPercent = currentBodyFatPercent,
                            targetWeight = targetWeight,
                            targetMuscleMass = targetMuscleMass,
                            targetBodyFatPercent = targetBodyFatPercent,
                            targetCalories = targetCalories,
                            targetCarbsGram = targetCarbsGram,
                            proteinGoal = proteinGoal,
                            targetFatGram = targetFatGram,
                            targetFiberGram = targetFiberGram,
                            targetSugarGram = targetSugarGram,
                            targetSodiumMilligram = targetSodiumMilligram,
                            manualTargetsEnabled = manualTargetsEnabled
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate AI Goal Proposal")
            }
            }
        }
    }
}

@Composable
private fun GoalProgressOverview(
    goalPlan: GoalPlanEntity,
    latestBodyMeasurement: BodyMeasurementEntity?
) {
    val latestGoalMeasurement = latestBodyMeasurement
        ?.takeIf { it.measuredEpochDay >= goalPlan.validFromEpochDay }
    val todayEpochDay = LocalDate.now().toEpochDay()
    val totalDays = (goalPlan.validToEpochDay - goalPlan.validFromEpochDay).coerceAtLeast(1L)
    val elapsedDays = (todayEpochDay - goalPlan.validFromEpochDay + 1L)
        .coerceIn(0L, totalDays)
    val periodProgress = elapsedDays / totalDays.toFloat()
    val startDateText = LocalDate.ofEpochDay(goalPlan.validFromEpochDay).toString()
    val endDateText = LocalDate.ofEpochDay(goalPlan.validToEpochDay - 1L).toString()
    val latestDateText = latestGoalMeasurement
        ?.let { LocalDate.ofEpochDay(it.measuredEpochDay).toString() }

    GoalCard(title = "Current Goal") {
        Text(
            text = "$startDateText ~ $endDateText",
            style = MaterialTheme.typography.bodyMedium,
            color = AppTextMuted
        )
        GoalProgressLabel(
            label = "Period",
            value = "$elapsedDays / $totalDays days"
        )
        GoalOverviewProgressBar(
            progress = periodProgress,
            color = GoalProgressColor,
            modifier = Modifier.height(12.dp)
        )
        if (goalPlan.planSummary.isNotBlank()) {
            Text(
                text = goalPlan.planSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextMuted
            )
        }
    }

    GoalCard(title = "Body Progress") {
        latestDateText?.let { measuredDate ->
            Text(
                text = "Last body status: $measuredDate",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextMuted
            )
        }
        GoalTargetProgressRow(
            label = "Weight",
            start = goalPlan.startWeightKg,
            current = latestGoalMeasurement?.weightKg ?: goalPlan.startWeightKg,
            target = goalPlan.targetWeightKg,
            unit = "kg",
            color = WeightProgressColor
        )
        if (
            goalPlan.startMuscleMassKg != null ||
            latestGoalMeasurement?.muscleMassKg != null ||
            goalPlan.targetMuscleMassKg != null
        ) {
            GoalTargetProgressRow(
                label = "Skeletal Muscle",
                start = goalPlan.startMuscleMassKg,
                current = latestGoalMeasurement?.muscleMassKg ?: goalPlan.startMuscleMassKg,
                target = goalPlan.targetMuscleMassKg,
                unit = "kg",
                color = MuscleProgressColor
            )
        }
        if (
            goalPlan.startBodyFatPercent != null ||
            latestGoalMeasurement?.bodyFatPercent != null ||
            goalPlan.targetBodyFatPercent != null
        ) {
            GoalTargetProgressRow(
                label = "Body Fat",
                start = goalPlan.startBodyFatPercent,
                current = latestGoalMeasurement?.bodyFatPercent ?: goalPlan.startBodyFatPercent,
                target = goalPlan.targetBodyFatPercent,
                unit = "%",
                color = BodyFatProgressColor
            )
        }
    }

    GoalCard(title = "Daily Targets") {
        GoalMetricCard(
            label = "Calories",
            value = "${goalPlan.dailyCalories} kcal",
            color = GoalProgressColor,
            modifier = Modifier.fillMaxWidth()
        )
        GoalFieldRow {
            GoalMetricCard("Carbs", "${goalPlan.dailyCarbsGram}g", MuscleProgressColor, Modifier.weight(1f))
            GoalMetricCard("Protein", "${goalPlan.dailyProteinGram}g", WeightProgressColor, Modifier.weight(1f))
            GoalMetricCard("Fat", "${goalPlan.dailyFatGram}g", BodyFatProgressColor, Modifier.weight(1f))
        }
        GoalFieldRow {
            GoalMetricCard("Fiber", "${goalPlan.dailyFiberGram}g", WeightProgressColor, Modifier.weight(1f))
            GoalMetricCard("Sugar", "${goalPlan.dailySugarGram}g", AppWarning, Modifier.weight(1f))
            GoalMetricCard("Sodium", "${goalPlan.dailySodiumMilligram}mg", AppTextMuted, Modifier.weight(1f))
        }
    }

}

@Composable
private fun GoalProposalCard(
    proposal: GoalPlanProposal,
    onEditDetailsClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val draft = proposal.draft
    val startDate = LocalDate.ofEpochDay(draft.validFromEpochDay).toString()
    val endDate = LocalDate.ofEpochDay(draft.validToEpochDay - 1L).toString()
    val estimatedText = proposal.estimatedFields.joinToString(" · ")

    GoalCard(title = "AI Goal Proposal") {
        NutritionPlanRow(
            label = "Period",
            value = "$startDate ~ $endDate · ${proposal.durationWeeks} weeks"
        )
        NutritionPlanRow(
            label = "Body goal",
            value = "Weight ${formatGoalMetric(draft.startWeightKg, "kg")} to ${formatGoalMetric(draft.targetWeightKg, "kg")} · " +
                "Muscle ${formatGoalMetric(draft.startMuscleMassKg, "kg")} to ${formatGoalMetric(draft.targetMuscleMassKg, "kg")} · " +
                "Body fat ${formatGoalMetric(draft.startBodyFatPercent, "%")} to ${formatGoalMetric(draft.targetBodyFatPercent, "%")}"
        )
        NutritionPlanRow(
            label = "Daily targets",
            value = "${draft.dailyCalories} kcal · Carbs ${draft.dailyCarbsGram}g · Protein ${draft.dailyProteinGram}g · Fat ${draft.dailyFatGram}g"
        )
        if (estimatedText.isNotBlank()) {
            Text(
                text = "Estimated: $estimatedText",
                style = MaterialTheme.typography.bodySmall,
                color = AppTextMuted
            )
        }
        Text(
            text = draft.planSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = AppSuccess
        )
        GoalFieldRow {
            OutlinedButton(
                onClick = onEditDetailsClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit Details")
            }
            Button(
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Confirm Goal")
            }
        }
    }
}

@Composable
private fun GoalProgressLabel(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GoalTargetProgressRow(
    label: String,
    start: Double?,
    current: Double?,
    target: Double?,
    unit: String,
    color: Color
) {
    val progress = buildTargetProgress(start, current, target)
    val targetGap = buildTargetGapText(current, target, unit)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GoalProgressLabel(
            label = label,
            value = "${formatGoalMetric(current, unit)} / ${formatGoalMetric(target, unit)}"
        )
        if (progress != null) {
            GoalOverviewProgressBar(
                progress = progress,
                color = color,
                modifier = Modifier.height(8.dp)
            )
        }
        Text(
            text = targetGap ?: "Target not set",
            style = MaterialTheme.typography.bodySmall,
            color = AppTextMuted
        )
    }
}

@Composable
private fun GoalOverviewProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(GoalTrackColor)
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(color)
            )
        }
    }
}

@Composable
private fun GoalCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppOutline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun BodyMeasurementForm(
    measuredDate: String,
    onMeasuredDateChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    metabolicRate: String,
    onMetabolicRateChange: (String) -> Unit,
    muscleMass: String,
    onMuscleMassChange: (String) -> Unit,
    bodyFatMass: String,
    onBodyFatMassChange: (String) -> Unit,
    bodyFatPercent: String,
    onBodyFatPercentChange: (String) -> Unit,
    errorMessage: String?,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoalDateField(
            value = measuredDate,
            onValueChange = onMeasuredDateChange,
            label = "Measured Date",
            isError = errorMessage?.contains("date", ignoreCase = true) == true,
            modifier = Modifier.fillMaxWidth()
        )
        GoalFieldRow {
            GoalNumberField(
                value = weight,
                onValueChange = onWeightChange,
                label = "Weight",
                suffix = "kg",
                modifier = Modifier.weight(1f)
            )
            GoalNumberField(
                value = metabolicRate,
                onValueChange = { onMetabolicRateChange(it.filter(Char::isDigit)) },
                label = "Metabolic Rate",
                suffix = "kcal",
                allowDecimal = false,
                modifier = Modifier.weight(1f)
            )
        }
        GoalFieldRow {
            GoalNumberField(
                value = muscleMass,
                onValueChange = onMuscleMassChange,
                label = "Skeletal Muscle",
                suffix = "kg",
                modifier = Modifier.weight(1f)
            )
            GoalNumberField(
                value = bodyFatMass,
                onValueChange = onBodyFatMassChange,
                label = "Body Fat Mass",
                suffix = "kg",
                modifier = Modifier.weight(1f)
            )
        }
        GoalNumberField(
            value = bodyFatPercent,
            onValueChange = onBodyFatPercentChange,
            label = "Body Fat %",
            suffix = "%",
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Only one value is required. Weight and body fat mass can estimate body fat percent for the AI plan.",
            style = MaterialTheme.typography.bodySmall,
            color = AppTextMuted
        )
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        GoalFieldRow {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Body Status")
            }
        }
    }
}

@Composable
private fun GoalFieldRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun GoalNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suffix: String,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = true,
    isRequiredError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (allowDecimal) {
                onValueChange(filterDecimalInput(it))
            } else {
                onValueChange(it.filter(Char::isDigit))
            }
        },
        label = { Text(label) },
        suffix = { Text(suffix) },
        isError = isRequiredError,
        supportingText = if (isRequiredError) {
            { Text("Required") }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier
    )
}

@Composable
private fun GoalDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("YYYY-MM-DD") },
        isError = isError,
        singleLine = true,
        supportingText = if (isError) {
            { Text("Check date") }
        } else {
            null
        },
        modifier = modifier
    )
}

@Composable
private fun PeriodModeButton(
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
private fun NutritionPlanRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GoalMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = AppTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private data class GoalNutritionPlan(
    val calories: Int,
    val carbsGram: Int,
    val proteinGram: Int,
    val fatGram: Int,
    val fiberGram: Int,
    val sugarGram: Int,
    val sodiumMilligram: Int,
    val summary: String
)

private data class GoalDateRange(
    val validFromEpochDay: Long,
    val validToEpochDay: Long,
    val durationDays: Long,
    val durationWeeks: Int,
    val startDateText: String,
    val endDateText: String
)

private data class GoalPlanProposal(
    val draft: GoalPlanDraft,
    val durationWeeks: Int,
    val estimatedFields: List<String>
)

private fun BodyMeasurementEntity.summaryText(): String {
    val measuredDate = LocalDate.ofEpochDay(measuredEpochDay).toString()
    val values = buildList {
        weightKg?.let { add("${it.toGoalInputText()}kg") }
        basalMetabolicRateKcal?.let { add("${it}kcal BMR") }
        muscleMassKg?.let { add("${it.toGoalInputText()}kg muscle") }
        bodyFatMassKg?.let { add("${it.toGoalInputText()}kg fat") }
        bodyFatPercent?.let { add("${it.toGoalInputText()}% fat") }
    }
    return if (values.isEmpty()) {
        "Latest body status: $measuredDate"
    } else {
        "Latest body status: $measuredDate · ${values.joinToString(" · ")}"
    }
}

private fun BodyMeasurementEntity.toDraft(): BodyMeasurementDraft {
    return BodyMeasurementDraft(
        measuredEpochDay = measuredEpochDay,
        weightKg = weightKg,
        muscleMassKg = muscleMassKg,
        basalMetabolicRateKcal = basalMetabolicRateKcal,
        bodyFatMassKg = bodyFatMassKg,
        bodyFatPercent = bodyFatPercent
    )
}

private fun parseMeasurementEpochDay(measuredDate: String): Long? {
    return try {
        LocalDate.parse(measuredDate.trim()).toEpochDay()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun bodyFatPercentFromMass(
    bodyFatMassKg: Double?,
    weightKg: Double?
): Double? {
    if (bodyFatMassKg == null || weightKg == null || weightKg <= 0.0) {
        return null
    }
    return ((bodyFatMassKg / weightKg) * 100.0).coerceIn(0.0, 80.0)
}

private fun suggestTargetBodyFatPercent(currentBodyFatPercent: Double): Double {
    val reduction = when {
        currentBodyFatPercent >= 30.0 -> 4.0
        currentBodyFatPercent >= 24.0 -> 3.0
        currentBodyFatPercent >= 18.0 -> 2.0
        else -> 0.0
    }
    return (currentBodyFatPercent - reduction).coerceAtLeast(10.0)
}

private fun suggestTargetWeight(
    currentWeightKg: Double?,
    currentBodyFatPercent: Double?,
    targetBodyFatPercent: Double?
): Double? {
    if (
        currentWeightKg == null ||
        currentBodyFatPercent == null ||
        targetBodyFatPercent == null ||
        targetBodyFatPercent >= 100.0
    ) {
        return null
    }
    val leanMassKg = currentWeightKg * (1.0 - currentBodyFatPercent / 100.0)
    return (leanMassKg / (1.0 - targetBodyFatPercent / 100.0))
        .takeIf { it > 0.0 && it.isFinite() }
}

private fun estimateBodyFatPercent(
    currentWeightKg: Double,
    targetWeightKg: Double
): Double {
    val baseline = when {
        currentWeightKg >= 90.0 -> 28.0
        currentWeightKg >= 75.0 -> 24.0
        currentWeightKg >= 60.0 -> 21.0
        else -> 18.0
    }
    val directionAdjustment = when {
        targetWeightKg < currentWeightKg -> 2.0
        targetWeightKg > currentWeightKg -> -1.0
        else -> 0.0
    }
    return roundGoalValue((baseline + directionAdjustment).coerceIn(10.0, 35.0))
}

private fun estimateSkeletalMuscleMass(
    weightKg: Double,
    bodyFatPercent: Double
): Double {
    val leanMassKg = weightKg * (1.0 - bodyFatPercent / 100.0)
    return roundGoalValue((leanMassKg * 0.56).coerceAtLeast(weightKg * 0.30))
}

private fun suggestTargetMuscleMass(
    startMuscleMassKg: Double,
    currentWeightKg: Double,
    targetWeightKg: Double
): Double {
    val weightChange = targetWeightKg - currentWeightKg
    val targetMuscleChange = if (weightChange > 0.0) {
        (weightChange * 0.30).coerceAtMost(2.5)
    } else {
        0.0
    }
    return roundGoalValue(startMuscleMassKg + targetMuscleChange)
}

private fun filterDecimalInput(value: String): String {
    var hasDot = false
    return value.filter { character ->
        when {
            character.isDigit() -> true
            character == '.' && !hasDot -> {
                hasDot = true
                true
            }
            else -> false
        }
    }
}

private fun parseGoalDateRange(
    startDate: String,
    endDate: String
): GoalDateRange? {
    return try {
        val start = LocalDate.parse(startDate.trim())
        val endInclusive = LocalDate.parse(endDate.trim())
        if (endInclusive < start) {
            null
        } else {
            val durationDays = endInclusive.toEpochDay() - start.toEpochDay() + 1L
            GoalDateRange(
                validFromEpochDay = start.toEpochDay(),
                validToEpochDay = endInclusive.plusDays(1).toEpochDay(),
                durationDays = durationDays,
                durationWeeks = ceil(durationDays / 7.0).roundToInt().coerceAtLeast(1),
                startDateText = start.toString(),
                endDateText = endInclusive.toString()
            )
        }
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun buildTargetDateFromDuration(
    startDate: String,
    durationWeeks: String
): String? {
    return try {
        val start = LocalDate.parse(startDate.trim())
        val weeks = durationWeeks.toIntOrNull()?.takeIf { it > 0 } ?: return null
        start.plusWeeks(weeks.toLong()).minusDays(1).toString()
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun buildGoalSummary(
    currentWeight: String,
    targetWeight: String,
    targetWeeks: String
): String {
    val current = currentWeight.toDoubleOrNull()
    val target = targetWeight.toDoubleOrNull()
    val weeks = targetWeeks.toIntOrNull()

    if (current == null || current <= 0.0) {
        return "Current weight is required before saving goals."
    }
    if (target == null || target <= 0.0 || weeks == null || weeks <= 0) {
        return "Enter target weight and duration to calculate your pace."
    }

    val change = target - current
    val totalChange = abs(change)

    if (change == 0.0) {
        return "Plan to maintain your current weight for $weeks weeks."
    }

    val direction = if (change < 0) "lose" else "gain"
    return "Plan to $direction ${formatWeight(totalChange)}kg over $weeks weeks - ${formatWeight(totalChange / weeks)}kg per week."
}

private fun buildGoalProposal(
    dateRange: GoalDateRange,
    currentWeight: String,
    currentMuscleMass: String,
    currentMetabolicRate: String,
    currentBodyFatPercent: String,
    targetWeight: String,
    targetMuscleMass: String,
    targetBodyFatPercent: String,
    targetCalories: String,
    targetCarbsGram: String,
    proteinGoal: String,
    targetFatGram: String,
    targetFiberGram: String,
    targetSugarGram: String,
    targetSodiumMilligram: String,
    manualTargetsEnabled: Boolean
): GoalPlanProposal? {
    val parsedCurrentWeight = currentWeight.toPositiveDoubleOrNull() ?: return null
    val providedTargetWeight = targetWeight.toPositiveDoubleOrNull()
    val proposedTargetWeight = providedTargetWeight ?: parsedCurrentWeight
    val providedStartBodyFat = currentBodyFatPercent.toNonNegativeDoubleOrNull()
        ?.coerceIn(5.0, 60.0)
    val proposedStartBodyFat = providedStartBodyFat
        ?: estimateBodyFatPercent(
            currentWeightKg = parsedCurrentWeight,
            targetWeightKg = proposedTargetWeight
        )
    val providedStartMuscle = currentMuscleMass.toPositiveDoubleOrNull()
    val proposedStartMuscle = providedStartMuscle
        ?: estimateSkeletalMuscleMass(
            weightKg = parsedCurrentWeight,
            bodyFatPercent = proposedStartBodyFat
        )
    val providedTargetBodyFat = targetBodyFatPercent.toNonNegativeDoubleOrNull()
        ?.coerceIn(5.0, 60.0)
    val proposedTargetBodyFat = providedTargetBodyFat
        ?: suggestTargetBodyFatPercent(proposedStartBodyFat)
    val providedTargetMuscle = targetMuscleMass.toPositiveDoubleOrNull()
    val proposedTargetMuscle = providedTargetMuscle
        ?: suggestTargetMuscleMass(
            startMuscleMassKg = proposedStartMuscle,
            currentWeightKg = parsedCurrentWeight,
            targetWeightKg = proposedTargetWeight
        )
    val proposalPlan = buildNutritionPlan(
        currentWeight = parsedCurrentWeight.toGoalInputText(),
        currentMuscleMass = proposedStartMuscle.toGoalInputText(),
        currentMetabolicRate = currentMetabolicRate,
        currentBodyFatPercent = proposedStartBodyFat.toGoalInputText(),
        targetWeight = proposedTargetWeight.toGoalInputText(),
        targetMuscleMass = proposedTargetMuscle.toGoalInputText(),
        targetBodyFatPercent = proposedTargetBodyFat.toGoalInputText(),
        targetWeeks = dateRange.durationWeeks.toString()
    )
    val estimatedFields = buildList {
        if (providedStartMuscle == null) {
            add("current muscle ${proposedStartMuscle.toGoalInputText()}kg")
        }
        if (providedStartBodyFat == null) {
            add("current body fat ${proposedStartBodyFat.toGoalInputText()}%")
        }
        if (providedTargetWeight == null) {
            add("target weight ${proposedTargetWeight.toGoalInputText()}kg")
        }
        if (providedTargetMuscle == null) {
            add("target muscle ${proposedTargetMuscle.toGoalInputText()}kg")
        }
        if (providedTargetBodyFat == null) {
            add("target body fat ${proposedTargetBodyFat.toGoalInputText()}%")
        }
    }
    val saveManualTargets = manualTargetsEnabled
    val baseSummary = buildSavedPlanSummary(proposalPlan, saveManualTargets)
    val planSummary = if (estimatedFields.isEmpty()) {
        baseSummary
    } else {
        "$baseSummary Estimated body composition included."
    }

    return GoalPlanProposal(
        draft = GoalPlanDraft(
            validFromEpochDay = dateRange.validFromEpochDay,
            validToEpochDay = dateRange.validToEpochDay,
            startWeightKg = parsedCurrentWeight,
            startMuscleMassKg = proposedStartMuscle,
            startBodyFatPercent = proposedStartBodyFat,
            targetWeightKg = proposedTargetWeight,
            targetMuscleMassKg = proposedTargetMuscle,
            targetBodyFatPercent = proposedTargetBodyFat,
            dailyCalories = dailyTargetValue(
                manualValue = targetCalories.toPositiveIntOrNull(),
                planValue = proposalPlan?.calories,
                defaultValue = 2000,
                useManualValue = saveManualTargets
            ),
            dailyCarbsGram = dailyTargetValue(
                manualValue = targetCarbsGram.toPositiveIntOrNull(),
                planValue = proposalPlan?.carbsGram,
                defaultValue = 250,
                useManualValue = saveManualTargets
            ),
            dailyProteinGram = dailyTargetValue(
                manualValue = proteinGoal.toPositiveIntOrNull(),
                planValue = proposalPlan?.proteinGram,
                defaultValue = 100,
                useManualValue = saveManualTargets
            ),
            dailyFatGram = dailyTargetValue(
                manualValue = targetFatGram.toPositiveIntOrNull(),
                planValue = proposalPlan?.fatGram,
                defaultValue = 60,
                useManualValue = saveManualTargets
            ),
            dailyFiberGram = dailyTargetValue(
                manualValue = targetFiberGram.toPositiveIntOrNull(),
                planValue = proposalPlan?.fiberGram,
                defaultValue = 25,
                useManualValue = saveManualTargets
            ),
            dailySugarGram = dailyTargetValue(
                manualValue = targetSugarGram.toPositiveIntOrNull(),
                planValue = proposalPlan?.sugarGram,
                defaultValue = 50,
                useManualValue = saveManualTargets
            ),
            dailySodiumMilligram = dailyTargetValue(
                manualValue = targetSodiumMilligram.toPositiveIntOrNull(),
                planValue = proposalPlan?.sodiumMilligram,
                defaultValue = 2300,
                useManualValue = saveManualTargets
            ),
            planSummary = planSummary,
            plannerVersion = if (saveManualTargets) {
                "manual-override-v1"
            } else {
                "local-goal-planner-v1"
            }
        ),
        durationWeeks = dateRange.durationWeeks,
        estimatedFields = estimatedFields
    )
}

private fun buildNutritionPlan(
    currentWeight: String,
    currentMuscleMass: String,
    currentMetabolicRate: String,
    currentBodyFatPercent: String,
    targetWeight: String,
    targetMuscleMass: String,
    targetBodyFatPercent: String,
    targetWeeks: String
): GoalNutritionPlan? {
    val current = currentWeight.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
    val target = targetWeight.toDoubleOrNull()?.takeIf { it > 0.0 } ?: current
    val weeks = targetWeeks.toIntOrNull()?.takeIf { it > 0 } ?: 8
    val currentMuscle = currentMuscleMass.toDoubleOrNull()?.takeIf { it > 0.0 }
    val basalMetabolicRate = currentMetabolicRate.toIntOrNull()?.takeIf { it > 0 }
    val targetMuscle = targetMuscleMass.toDoubleOrNull()?.takeIf { it > 0.0 }
    val currentBodyFat = currentBodyFatPercent.toDoubleOrNull()?.takeIf { it >= 0.0 }
    val targetBodyFat = targetBodyFatPercent.toDoubleOrNull()?.takeIf { it >= 0.0 }
    val weeklyWeightChange = (target - current) / weeks
    val dailyEnergyAdjustment = ((weeklyWeightChange * 7700.0) / 7.0)
        .roundToInt()
        .coerceIn(-700, 450)
    val muscleAdjustment = currentMuscle
        ?.let { ((it - current * 0.42) * 8.0).roundToInt() }
        ?: 0
    val bodyFatAdjustment = currentBodyFat
        ?.let { if (it >= 28.0) -80 else if (it <= 15.0) 70 else 0 }
        ?: 0
    val maintenanceBase = basalMetabolicRate
        ?.let { (it * 1.35).roundToInt() }
        ?: (current * 30.0).roundToInt()
    val maintenanceCalories = maintenanceBase + muscleAdjustment + bodyFatAdjustment
    val calories = (maintenanceCalories + dailyEnergyAdjustment).coerceIn(
        minimumValue = (current * 20.0).roundToInt(),
        maximumValue = (current * 38.0).roundToInt()
    )
    val muscleGainPlanned = targetMuscle != null && currentMuscle != null && targetMuscle > currentMuscle
    val fatLossPlanned = target < current ||
        (targetBodyFat != null && currentBodyFat != null && targetBodyFat < currentBodyFat)
    val proteinMultiplier = when {
        muscleGainPlanned -> 2.0
        fatLossPlanned -> 1.8
        else -> 1.6
    }
    val proteinGram = (current * proteinMultiplier).roundToInt().coerceIn(60, 220)
    val fatRatio = if (fatLossPlanned) 0.25 else 0.28
    val fatGram = ((calories * fatRatio) / 9.0).roundToInt().coerceIn(40, 100)
    val carbGram = max(80, ((calories - proteinGram * 4 - fatGram * 9) / 4.0).roundToInt())
    val fiberGram = max(25, ((calories / 1000.0) * 14.0).roundToInt())
    val sugarGram = min(50, max(25, ((calories * 0.10) / 4.0).roundToInt()))
    val sodiumMilligram = 2300
    val summary = buildPlanSummary(
        dailyEnergyAdjustment = dailyEnergyAdjustment,
        fatLossPlanned = fatLossPlanned,
        muscleGainPlanned = muscleGainPlanned
    )

    return GoalNutritionPlan(
        calories = calories,
        carbsGram = carbGram,
        proteinGram = proteinGram,
        fatGram = fatGram,
        fiberGram = fiberGram,
        sugarGram = sugarGram,
        sodiumMilligram = sodiumMilligram,
        summary = summary
    )
}

private fun buildPlanSummary(
    dailyEnergyAdjustment: Int,
    fatLossPlanned: Boolean,
    muscleGainPlanned: Boolean
): String {
    val energyText = when {
        dailyEnergyAdjustment < -50 -> "mild calorie deficit"
        dailyEnergyAdjustment > 50 -> "controlled calorie surplus"
        else -> "maintenance calorie range"
    }
    val bodyText = when {
        muscleGainPlanned -> "higher protein to support muscle gain"
        fatLossPlanned -> "higher protein to protect lean mass during fat loss"
        else -> "balanced macros for steady tracking"
    }
    return "AI plan: $energyText with $bodyText."
}

private fun dailyTargetValue(
    manualValue: Int?,
    planValue: Int?,
    defaultValue: Int,
    useManualValue: Boolean
): Int {
    return if (useManualValue) {
        manualValue ?: planValue ?: defaultValue
    } else {
        planValue ?: defaultValue
    }
}

private fun buildTargetProgress(
    start: Double?,
    current: Double?,
    target: Double?
): Float? {
    if (start == null || current == null || target == null) {
        return null
    }
    val totalChange = target - start
    if (abs(totalChange) < 0.0001) {
        return 1f
    }
    return ((current - start) / totalChange).toFloat().coerceIn(0f, 1f)
}

private fun buildTargetGapText(
    current: Double?,
    target: Double?,
    unit: String
): String? {
    if (current == null || target == null) {
        return null
    }
    val gap = target - current
    if (abs(gap) < 0.05) {
        return "Target reached"
    }
    return "Target gap: ${formatWeight(abs(gap))}$unit"
}

private fun formatGoalMetric(
    value: Double?,
    unit: String
): String {
    return value?.let { "${it.toGoalInputText()}$unit" } ?: "Not set"
}

private fun buildSavedPlanSummary(
    fallbackPlan: GoalNutritionPlan?,
    manualTargetsEnabled: Boolean
): String {
    val baseSummary = fallbackPlan?.summary ?: "AI plan could not be generated from the current input."
    return if (manualTargetsEnabled) {
        "$baseSummary Manual override applied."
    } else {
        baseSummary
    }
}

private fun String.hasPositiveNumber(): Boolean {
    return toDoubleOrNull()?.let { it > 0.0 } == true
}

private fun String.toPositiveDoubleOrNull(): Double? {
    return toDoubleOrNull()?.takeIf { it > 0.0 }
}

private fun String.toNonNegativeDoubleOrNull(): Double? {
    return toDoubleOrNull()?.takeIf { it >= 0.0 }
}

private fun String.toPositiveIntOrNull(): Int? {
    return toIntOrNull()?.takeIf { it > 0 }
}

private fun formatWeight(value: Double): String {
    return "%.1f".format(value)
}

private fun roundGoalValue(value: Double): Double {
    return (value * 10.0).roundToInt() / 10.0
}

private fun Double.toGoalInputText(): String {
    return if (abs(this % 1.0) < 0.0001) {
        toInt().toString()
    } else {
        "%.1f".format(this)
    }
}
