package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.util.UriImage
import com.jaewonlee.aidietrecord.ui.util.formatMealDateTime

@Composable
fun MealDetailScreen(
    mealRecord: MealRecord?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: (MealRecord) -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    ScreenScaffold(
        title = "Meal Details",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (mealRecord == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, AppOutline),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Meal record not found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
                return@Column
            }

            UriImage(
                imageUri = mealRecord.imageUri,
                placeholderText = if (mealRecord.imageUri == null) {
                    "No meal photo saved"
                } else {
                    "Unable to load meal photo"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = mealRecord.foodName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged at: ${formatMealDateTime(mealRecord.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextMuted
                    )
                    Text("${mealRecord.calories} kcal", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = mealRecord.memo.ifBlank { "No memo" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
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
                        text = "Food Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    mealRecord.foods.forEach { food ->
                        MealFoodInfoRow(food = food)
                    }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Nutrition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    NutritionInfoRow("Carbs", "${mealRecord.carbsGram}g")
                    NutritionInfoRow("Protein", "${mealRecord.proteinGram}g")
                    NutritionInfoRow("Fat", "${mealRecord.fatGram}g")
                    NutritionInfoRow("Fiber", "${mealRecord.fiberGram}g")
                    NutritionInfoRow("Sugar", "${mealRecord.sugarGram}g")
                    NutritionInfoRow("Sodium", "${mealRecord.sodiumMilligram}mg")
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AI Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Estimated food: ${mealRecord.aiFoodName ?: "None"}")
                    Text("Estimated calories: ${mealRecord.aiCalories ?: 0} kcal")
                    Text(
                        text = mealRecord.aiSummary ?: "No AI review saved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDeleteDialog && mealRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete meal?") },
            text = {
                Text("This meal record will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(mealRecord)
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MealFoodInfoRow(food: MealFoodRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = food.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${food.calories} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = AppSuccess
            )
        }
        Text(
            text = "Carbs ${food.carbsGram}g · Protein ${food.proteinGram}g · Fat ${food.fatGram}g",
            style = MaterialTheme.typography.bodySmall,
            color = AppTextMuted
        )
        Text(
            text = "Fiber ${food.fiberGram}g · Sugar ${food.sugarGram}g · Sodium ${food.sodiumMilligram}mg",
            style = MaterialTheme.typography.bodySmall,
            color = AppTextMuted
        )
    }
}

@Composable
private fun NutritionInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppSuccess
        )
    }
}
