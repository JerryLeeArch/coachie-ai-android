package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.data.nutrition.estimateCaloriesFromFoodName
import com.jaewonlee.aidietrecord.data.nutrition.estimateMacros

@Composable
fun AddMealScreen(
    initialMeal: MealRecord? = null,
    onBackClick: () -> Unit,
    onSaveClick: (MealUploadDraft) -> Unit
) {
    val isEditMode = initialMeal != null
    val foodDrafts = remember(initialMeal?.id) {
        mutableStateListOf<FoodDraftUiState>().apply {
            addAll(initialMeal.toFoodDrafts())
        }
    }
    var nextDraftId by remember(initialMeal?.id) {
        mutableStateOf((foodDrafts.maxOfOrNull { it.id } ?: 0L) + 1L)
    }
    var memo by remember(initialMeal?.id) { mutableStateOf(initialMeal?.memo.orEmpty()) }
    var errorMessage by remember(initialMeal?.id) { mutableStateOf<String?>(null) }

    val totalCalories = foodDrafts.sumOf { it.reviewedCalories() }
    val totalCarbsGram = foodDrafts.sumOf { it.reviewedCarbsGram() }
    val totalProteinGram = foodDrafts.sumOf { it.reviewedProteinGram() }
    val totalFatGram = foodDrafts.sumOf { it.reviewedFatGram() }

    fun updateDraft(
        draftId: Long,
        update: (FoodDraftUiState) -> FoodDraftUiState
    ) {
        val index = foodDrafts.indexOfFirst { it.id == draftId }
        if (index >= 0) {
            foodDrafts[index] = update(foodDrafts[index])
        }
    }

    ScreenScaffold(
        title = if (isEditMode) "음식 수정" else "식사 추가",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MealPhotoPicker()

            SectionHeader(
                title = "음식 목록",
                trailingText = "${foodDrafts.size}개"
            )

            foodDrafts.forEachIndexed { index, draft ->
                FoodDraftCard(
                    draft = draft,
                    index = index,
                    canRemove = foodDrafts.size > 1,
                    onFoodNameChange = { value ->
                        updateDraft(draft.id) { it.copy(foodName = value) }
                    },
                    onCaloriesChange = { value ->
                        updateDraft(draft.id) { it.copy(calories = value.filter(Char::isDigit)) }
                    },
                    onCarbsChange = { value ->
                        updateDraft(draft.id) { it.copy(carbsGram = value.filter(Char::isDigit)) }
                    },
                    onProteinChange = { value ->
                        updateDraft(draft.id) { it.copy(proteinGram = value.filter(Char::isDigit)) }
                    },
                    onFatChange = { value ->
                        updateDraft(draft.id) { it.copy(fatGram = value.filter(Char::isDigit)) }
                    },
                    onRemoveClick = {
                        foodDrafts.removeAll { it.id == draft.id }
                        errorMessage = null
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    foodDrafts.add(FoodDraftUiState(id = nextDraftId))
                    nextDraftId += 1
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("음식 추가")
            }

            OutlinedTextField(
                value = memo,
                onValueChange = { memo = it },
                label = { Text("식사 메모") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            MealTotalSummary(
                calories = totalCalories,
                carbsGram = totalCarbsGram,
                proteinGram = totalProteinGram,
                fatGram = totalFatGram
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val invalidIndex = foodDrafts.indexOfFirst { draft ->
                        draft.foodName.trim().isBlank()
                    }

                    errorMessage = when {
                        invalidIndex >= 0 -> "${invalidIndex + 1}번째 음식명을 입력해 주세요."
                        else -> null
                    }

                    if (errorMessage == null) {
                        val createdAt = initialMeal?.createdAt ?: System.currentTimeMillis()
                        onSaveClick(
                            MealUploadDraft(
                                id = initialMeal?.id ?: 0,
                                ownerId = initialMeal?.ownerId ?: 0,
                                memo = memo.trim(),
                                imageUri = initialMeal?.imageUri,
                                createdAt = createdAt,
                                foods = foodDrafts.map { it.toMealFoodDraft() }
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "수정 내용 저장" else "AI 검사 후 식사 업로드")
            }
        }
    }
}

@Composable
private fun MealPhotoPicker() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "음식 사진",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFEAF1E8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "이미지 미리보기",
                    color = Color(0xFF52624F),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            OutlinedButton(onClick = { }) {
                Text("사진 선택")
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    trailingText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = trailingText,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF52624F)
        )
    }
}

@Composable
private fun FoodDraftCard(
    draft: FoodDraftUiState,
    index: Int,
    canRemove: Boolean,
    onFoodNameChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "음식 ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (canRemove) {
                    TextButton(onClick = onRemoveClick) {
                        Text("삭제")
                    }
                }
            }

            OutlinedTextField(
                value = draft.foodName,
                onValueChange = onFoodNameChange,
                label = { Text("음식명") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = draft.calories,
                onValueChange = onCaloriesChange,
                label = { Text("칼로리") },
                placeholder = { Text("AI 자동") },
                suffix = { Text("kcal") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroTextField(
                    value = draft.carbsGram,
                    onValueChange = onCarbsChange,
                    label = "탄수",
                    modifier = Modifier.weight(1f)
                )
                MacroTextField(
                    value = draft.proteinGram,
                    onValueChange = onProteinChange,
                    label = "단백질",
                    modifier = Modifier.weight(1f)
                )
                MacroTextField(
                    value = draft.fatGram,
                    onValueChange = onFatChange,
                    label = "지방",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("자동") },
        suffix = { Text("g") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
private fun MealTotalSummary(
    calories: Int,
    carbsGram: Int,
    proteinGram: Int,
    fatGram: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "식사 합계",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$calories kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D6A4F)
                )
            }
            HorizontalDivider(color = Color(0xFFE1E7DF))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill("탄수", "${carbsGram}g", Modifier.weight(1f))
                SummaryPill("단백질", "${proteinGram}g", Modifier.weight(1f))
                SummaryPill("지방", "${fatGram}g", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFFF4F7F2), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF52624F)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class FoodDraftUiState(
    val id: Long,
    val foodName: String = "",
    val calories: String = "",
    val carbsGram: String = "",
    val proteinGram: String = "",
    val fatGram: String = ""
)

private fun MealRecord?.toFoodDrafts(): List<FoodDraftUiState> {
    val existingFoods = this?.foods.orEmpty()
    if (existingFoods.isEmpty()) {
        return listOf(FoodDraftUiState(id = 1L))
    }

    return existingFoods.mapIndexed { index, food ->
        FoodDraftUiState(
            id = index + 1L,
            foodName = food.foodName,
            calories = food.calories.toString(),
            carbsGram = food.carbsGram.toString(),
            proteinGram = food.proteinGram.toString(),
            fatGram = food.fatGram.toString()
        )
    }
}

private fun FoodDraftUiState.toMealFoodDraft(): MealFoodDraft {
    return MealFoodDraft(
        foodName = foodName.trim(),
        calories = calories.toPositiveIntOrNull(),
        carbsGram = carbsGram.toIntOrNull(),
        proteinGram = proteinGram.toIntOrNull(),
        fatGram = fatGram.toIntOrNull()
    )
}

private fun FoodDraftUiState.reviewedCalories(): Int {
    return calories.toPositiveIntOrNull() ?: estimateCaloriesFromFoodName(foodName)
}

private fun FoodDraftUiState.reviewedCarbsGram(): Int {
    return carbsGram.toIntOrNull() ?: estimateMacros(reviewedCalories()).carbsGram
}

private fun FoodDraftUiState.reviewedProteinGram(): Int {
    return proteinGram.toIntOrNull() ?: estimateMacros(reviewedCalories()).proteinGram
}

private fun FoodDraftUiState.reviewedFatGram(): Int {
    return fatGram.toIntOrNull() ?: estimateMacros(reviewedCalories()).fatGram
}

private fun String.toPositiveIntOrNull(): Int? {
    return toIntOrNull()?.takeIf { it > 0 }
}
