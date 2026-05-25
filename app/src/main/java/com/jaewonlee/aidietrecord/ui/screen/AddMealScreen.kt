package com.jaewonlee.aidietrecord.ui.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodDraft
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.data.model.MealUploadDraft
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.util.UriImage

@Composable
fun AddMealScreen(
    initialMeal: MealRecord? = null,
    isSaving: Boolean = false,
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
    var errorMessage by remember(initialMeal?.id) { mutableStateOf<String?>(null) }

    if (!isEditMode) {
        SingleMealCaptureScreen(
            isSaving = isSaving,
            onBackClick = onBackClick,
            onSaveClick = onSaveClick
        )
        return
    }

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
        title = if (isEditMode) "Edit Meal" else "Add Meal",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Food Items",
                trailingText = itemCountText(foodDrafts.size)
            )

            foodDrafts.forEachIndexed { index, draft ->
                key(draft.id) {
                    FoodDraftCard(
                        draft = draft,
                        index = index,
                        canRemove = foodDrafts.size > 1,
                        showNutritionFields = isEditMode,
                        onFoodNameChange = { value ->
                            updateDraft(draft.id) { it.copy(foodName = value) }
                        },
                        onDescriptionChange = { value ->
                            updateDraft(draft.id) { it.copy(description = value) }
                        },
                        onImageSelected = { value ->
                            updateDraft(draft.id) { it.copy(imageUri = value) }
                            errorMessage = null
                        },
                        onCaloriesChange = { value ->
                            updateDraft(draft.id) { it.copy(calories = value) }
                        },
                        onCarbsChange = { value ->
                            updateDraft(draft.id) { it.copy(carbsGram = value) }
                        },
                        onProteinChange = { value ->
                            updateDraft(draft.id) { it.copy(proteinGram = value) }
                        },
                        onFatChange = { value ->
                            updateDraft(draft.id) { it.copy(fatGram = value) }
                        },
                        onFiberChange = { value ->
                            updateDraft(draft.id) { it.copy(fiberGram = value) }
                        },
                        onSugarChange = { value ->
                            updateDraft(draft.id) { it.copy(sugarGram = value) }
                        },
                        onSodiumChange = { value ->
                            updateDraft(draft.id) { it.copy(sodiumMilligram = value) }
                        },
                        onRemoveClick = {
                            foodDrafts.removeAll { it.id == draft.id }
                            errorMessage = null
                        }
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    foodDrafts.add(FoodDraftUiState(id = nextDraftId))
                    nextDraftId += 1
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Another Food")
            }

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
                        draft.foodName.isBlank() &&
                            draft.description.isBlank() &&
                            draft.imageUri == null
                    }

                    errorMessage = when {
                        invalidIndex >= 0 -> {
                            "Add a food name, description, or photo for item ${invalidIndex + 1}."
                        }
                        else -> null
                    }

                    if (errorMessage == null) {
                        val createdAt = initialMeal?.createdAt ?: System.currentTimeMillis()
                        val foods = foodDrafts.map { it.toMealFoodDraft() }
                        onSaveClick(
                            MealUploadDraft(
                                id = initialMeal?.id ?: 0,
                                ownerId = initialMeal?.ownerId ?: 0,
                                memo = foods.toMealMemo(initialMeal?.memo.orEmpty()),
                                imageUri = foods.firstNotNullOfOrNull { it.imageUri },
                                createdAt = createdAt,
                                foods = foods
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isSaving -> "Analyzing..."
                        isEditMode -> "Save Changes"
                        else -> "Analyze Meal"
                    }
                )
            }
        }
    }
}

@Composable
private fun SingleMealCaptureScreen(
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (MealUploadDraft) -> Unit
) {
    val context = LocalContext.current
    var mealImageUri by remember { mutableStateOf<String?>(null) }
    var mealNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { selectedUri ->
        if (selectedUri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            mealImageUri = selectedUri.toString()
            errorMessage = null
        }
    }

    ScreenScaffold(
        title = "Add Meal",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Meal Input",
                trailingText = "AI split"
            )

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
                    mealImageUri?.let { imageUri ->
                        UriImage(
                            imageUri = imageUri,
                            placeholderText = "Unable to load meal photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                            Text(if (mealImageUri == null) "Add Photo" else "Change Photo")
                        }
                        if (mealImageUri != null) {
                            TextButton(onClick = { mealImageUri = null }) {
                                Text("Remove Photo")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mealNotes,
                        onValueChange = {
                            mealNotes = it
                            errorMessage = null
                        },
                        label = { Text("Meal Description") },
                        placeholder = {
                            Text(
                                "e.g. Kimchi stew, half bowl of rice, egg roll, and a little spicy pork"
                            )
                        },
                        minLines = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val trimmedNotes = mealNotes.trim()
                    if (trimmedNotes.isBlank() && mealImageUri == null) {
                        errorMessage = "Add a photo or describe the meal."
                    } else {
                        onSaveClick(
                            MealUploadDraft(
                                memo = trimmedNotes,
                                imageUri = mealImageUri,
                                createdAt = System.currentTimeMillis(),
                                foods = listOf(
                                    MealFoodDraft(
                                        foodName = "",
                                        description = trimmedNotes,
                                        imageUri = mealImageUri
                                    )
                                )
                            )
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Analyzing..." else "Analyze Meal")
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
            color = AppTextMuted
        )
    }
}

@Composable
private fun FoodDraftCard(
    draft: FoodDraftUiState,
    index: Int,
    canRemove: Boolean,
    showNutritionFields: Boolean,
    onFoodNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageSelected: (String?) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onFiberChange: (String) -> Unit,
    onSugarChange: (String) -> Unit,
    onSodiumChange: (String) -> Unit,
    onRemoveClick: () -> Unit
) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { selectedUri ->
        if (selectedUri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    selectedUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            onImageSelected(selectedUri.toString())
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Food ${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (canRemove) {
                    TextButton(onClick = onRemoveClick) {
                        Text("Remove")
                    }
                }
            }

            UriImage(
                imageUri = draft.imageUri,
                placeholderText = "Food photo optional",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                    Text(if (draft.imageUri == null) "Add Photo" else "Change Photo")
                }
                if (draft.imageUri != null) {
                    TextButton(onClick = { onImageSelected(null) }) {
                        Text("Remove Photo")
                    }
                }
            }

            OutlinedTextField(
                value = draft.foodName,
                onValueChange = onFoodNameChange,
                label = { Text("Food Name") },
                placeholder = { Text("e.g. Chicken salad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = draft.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                placeholder = {
                    Text(
                        "Example: Grilled chicken salad with avocado and light dressing. " +
                            "Medium bowl, no cheese. Add any calories or nutrition details you know."
                    )
                },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            if (showNutritionFields) {
                Text(
                    text = "Nutrition Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                NutritionFieldRow {
                    NutritionNumberField(
                        label = "Calories (kcal)",
                        value = draft.calories,
                        onValueChange = onCaloriesChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionNumberField(
                        label = "Carbs (g)",
                        value = draft.carbsGram,
                        onValueChange = onCarbsChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                NutritionFieldRow {
                    NutritionNumberField(
                        label = "Protein (g)",
                        value = draft.proteinGram,
                        onValueChange = onProteinChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionNumberField(
                        label = "Fat (g)",
                        value = draft.fatGram,
                        onValueChange = onFatChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                NutritionFieldRow {
                    NutritionNumberField(
                        label = "Fiber (g)",
                        value = draft.fiberGram,
                        onValueChange = onFiberChange,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionNumberField(
                        label = "Sugar (g)",
                        value = draft.sugarGram,
                        onValueChange = onSugarChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                NutritionNumberField(
                    label = "Sodium (mg)",
                    value = draft.sodiumMilligram,
                    onValueChange = onSodiumChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun NutritionFieldRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun NutritionNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isWholeNumberInput()) {
                onValueChange(input)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}

private data class FoodDraftUiState(
    val id: Long,
    val foodName: String = "",
    val description: String = "",
    val imageUri: String? = null,
    val calories: String = "",
    val carbsGram: String = "",
    val proteinGram: String = "",
    val fatGram: String = "",
    val fiberGram: String = "",
    val sugarGram: String = "",
    val sodiumMilligram: String = ""
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
            description = food.description,
            imageUri = food.imageUri ?: this?.imageUri?.takeIf { index == 0 },
            calories = food.calories.toString(),
            carbsGram = food.carbsGram.toString(),
            proteinGram = food.proteinGram.toString(),
            fatGram = food.fatGram.toString(),
            fiberGram = food.fiberGram.toString(),
            sugarGram = food.sugarGram.toString(),
            sodiumMilligram = food.sodiumMilligram.toString()
        )
    }
}

private fun FoodDraftUiState.toMealFoodDraft(): MealFoodDraft {
    return MealFoodDraft(
        foodName = foodName.trim(),
        description = description.trim(),
        imageUri = imageUri,
        calories = calories.toNullableInt(),
        carbsGram = carbsGram.toNullableInt(),
        proteinGram = proteinGram.toNullableInt(),
        fatGram = fatGram.toNullableInt(),
        fiberGram = fiberGram.toNullableInt(),
        sugarGram = sugarGram.toNullableInt(),
        sodiumMilligram = sodiumMilligram.toNullableInt()
    )
}

private fun List<MealFoodDraft>.toMealMemo(fallbackMemo: String): String {
    return mapIndexedNotNull { index, food ->
        food.description
            .takeIf { it.isNotBlank() }
            ?.let { "Food ${index + 1}: $it" }
    }
        .joinToString("\n\n")
        .ifBlank { fallbackMemo }
}

private fun itemCountText(count: Int): String {
    return if (count == 1) "1 item" else "$count items"
}

private fun String.isWholeNumberInput(): Boolean {
    return all { it.isDigit() }
}

private fun String.toNullableInt(): Int? {
    return trim().takeIf { it.isNotBlank() }?.toIntOrNull()
}
