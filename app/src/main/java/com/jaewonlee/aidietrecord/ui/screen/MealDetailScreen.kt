package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppSurfaceSoft
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted
import com.jaewonlee.aidietrecord.ui.theme.MacroCarb
import com.jaewonlee.aidietrecord.ui.theme.MacroFat
import com.jaewonlee.aidietrecord.ui.theme.MacroFiber
import com.jaewonlee.aidietrecord.ui.theme.MacroProtein
import com.jaewonlee.aidietrecord.ui.theme.MacroSodium
import com.jaewonlee.aidietrecord.ui.theme.MacroSugar
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
                    "No photo"
                } else {
                    "Unable to load meal photo"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (mealRecord.imageUri == null) 136.dp else 220.dp)
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
                        text = "Meal summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTextMuted
                    )
                    Text(
                        text = mealRecord.foodName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Logged at: ${formatMealDateTime(mealRecord.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NutritionMetricPill(
                            label = "Calories",
                            value = "${mealRecord.calories} kcal",
                            color = AppSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        NutritionMetricPill(
                            label = "Foods",
                            value = "${mealRecord.foods.size}",
                            color = AppTextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }
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

            SectionTitle(text = "Food Items")
            mealRecord.foods.forEach { food ->
                MealFoodCard(food = food)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NutritionMetricPill("Carbs", "${mealRecord.carbsGram}g", MacroCarb, Modifier.weight(1f))
                        NutritionMetricPill("Protein", "${mealRecord.proteinGram}g", MacroProtein, Modifier.weight(1f))
                        NutritionMetricPill("Fat", "${mealRecord.fatGram}g", MacroFat, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NutritionMetricPill("Fiber", "${mealRecord.fiberGram}g", MacroFiber, Modifier.weight(1f))
                        NutritionMetricPill("Sugar", "${mealRecord.sugarGram}g", MacroSugar, Modifier.weight(1f))
                        NutritionMetricPill("Sodium", "${mealRecord.sodiumMilligram}mg", MacroSodium, Modifier.weight(1f))
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun MealFoodCard(food: MealFoodRecord) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppOutline),
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
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = food.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${food.calories} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppSuccess,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
            if (food.description.isNotBlank()) {
                Text(
                    text = food.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTextMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NutritionMetricPill("Carbs", "${food.carbsGram}g", MacroCarb, Modifier.weight(1f))
                NutritionMetricPill("Protein", "${food.proteinGram}g", MacroProtein, Modifier.weight(1f))
                NutritionMetricPill("Fat", "${food.fatGram}g", MacroFat, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun NutritionMetricPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AppSurfaceSoft, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AppTextMuted,
                maxLines = 1
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
