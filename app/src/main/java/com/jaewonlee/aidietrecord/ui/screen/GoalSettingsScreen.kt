package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun GoalSettingsScreen(
    currentWeight: String,
    onCurrentWeightChange: (String) -> Unit,
    targetWeight: String,
    onTargetWeightChange: (String) -> Unit,
    targetWeeks: String,
    onTargetWeeksChange: (String) -> Unit,
    targetCalories: String,
    onTargetCaloriesChange: (String) -> Unit,
    proteinGoal: String,
    onProteinGoalChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val goalSummary = buildGoalSummary(currentWeight, targetWeight, targetWeeks)

    ScreenScaffold(
        title = "Goal Settings",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Set your weight pace and daily nutrition targets together.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Goal Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currentWeight,
                            onValueChange = { onCurrentWeightChange(filterDecimalInput(it)) },
                            label = { Text("Current Weight") },
                            suffix = { Text("kg") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetWeight,
                            onValueChange = { onTargetWeightChange(filterDecimalInput(it)) },
                            label = { Text("Target Weight") },
                            suffix = { Text("kg") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = targetWeeks,
                        onValueChange = { onTargetWeeksChange(it.filter(Char::isDigit)) },
                        label = { Text("Goal Duration") },
                        suffix = { Text("weeks") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = goalSummary,
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
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Daily Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = targetCalories,
                        onValueChange = { onTargetCaloriesChange(it.filter(Char::isDigit)) },
                        label = { Text("Calorie Goal") },
                        suffix = { Text("kcal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = proteinGoal,
                        onValueChange = { onProteinGoalChange(it.filter(Char::isDigit)) },
                        label = { Text("Protein Goal") },
                        suffix = { Text("g") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Goals")
            }
        }
    }
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

private fun buildGoalSummary(
    currentWeight: String,
    targetWeight: String,
    targetWeeks: String
): String {
    val current = currentWeight.toDoubleOrNull()
    val target = targetWeight.toDoubleOrNull()
    val weeks = targetWeeks.toIntOrNull()

    if (current == null || target == null || weeks == null || weeks <= 0) {
        return "Enter your target weight and duration to calculate your pace."
    }

    val change = target - current
    val totalChange = abs(change)

    if (change == 0.0) {
        return "Plan to maintain your current weight for $weeks weeks."
    }

    val direction = if (change < 0) "lose" else "gain"
    return "Plan to $direction ${formatWeight(totalChange)}kg over $weeks weeks - ${formatWeight(totalChange / weeks)}kg per week."
}

private fun formatWeight(value: Double): String {
    return "%.1f".format(value)
}
