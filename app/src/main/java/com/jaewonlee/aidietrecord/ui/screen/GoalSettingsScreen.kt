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
        title = "내 목표 설정",
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
                text = "체중 변화 속도와 영양 기준을 함께 설정합니다.",
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
                        text = "목표 기간",
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
                            label = { Text("현재 체중") },
                            suffix = { Text("kg") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetWeight,
                            onValueChange = { onTargetWeightChange(filterDecimalInput(it)) },
                            label = { Text("목표 체중") },
                            suffix = { Text("kg") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = targetWeeks,
                        onValueChange = { onTargetWeeksChange(it.filter(Char::isDigit)) },
                        label = { Text("목표 기간") },
                        suffix = { Text("주") },
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
                        text = "하루 기준",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = targetCalories,
                        onValueChange = { onTargetCaloriesChange(it.filter(Char::isDigit)) },
                        label = { Text("목표 칼로리") },
                        suffix = { Text("kcal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = proteinGoal,
                        onValueChange = { onProteinGoalChange(it.filter(Char::isDigit)) },
                        label = { Text("단백질 목표") },
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
                Text("목표 저장")
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
        return "목표 체중과 기간을 입력하면 변화 속도를 계산합니다."
    }

    val change = target - current
    val direction = when {
        change < 0 -> "감량"
        change > 0 -> "증량"
        else -> "유지"
    }
    val totalChange = abs(change)

    if (direction == "유지") {
        return "${weeks}주 동안 현재 체중을 유지하는 계획입니다."
    }

    return "${weeks}주 동안 ${formatWeight(totalChange)}kg $direction · 주당 ${formatWeight(totalChange / weeks)}kg 변화"
}

private fun formatWeight(value: Double): String {
    return "%.1f".format(value)
}
