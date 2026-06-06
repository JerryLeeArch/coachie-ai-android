package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.theme.AppSuccess
import com.jaewonlee.aidietrecord.ui.util.UriImage

@Composable
fun MealReviewScreen(
    reviewedMeal: MealRecord?,
    isAnalyzing: Boolean,
    errorMessage: String?,
    onConfirmClick: (MealRecord) -> Unit,
    onRetryClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onBackClick: () -> Unit
) {
    ScreenScaffold(
        title = "Review Meal",
        onBackClick = onBackClick
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                isAnalyzing -> AnalyzingMealCard()
                errorMessage != null -> MealReviewErrorCard(
                    message = errorMessage,
                    onRetryClick = onRetryClick,
                    onDiscardClick = onDiscardClick
                )
                reviewedMeal == null -> MealReviewEmptyCard(onDiscardClick = onDiscardClick)
                else -> MealReviewResult(
                    reviewedMeal = reviewedMeal,
                    onConfirmClick = onConfirmClick,
                    onDiscardClick = onDiscardClick
                )
            }
        }
    }
}

@Composable
private fun AnalyzingMealCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(
                text = "AI is analyzing your meal.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "The meal will not be saved until you confirm the result.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MealReviewErrorCard(
    message: String,
    onRetryClick: () -> Unit,
    onDiscardClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Analysis failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            ReviewActionButtons(
                primaryText = "Retry",
                secondaryText = "Discard",
                onPrimaryClick = onRetryClick,
                onSecondaryClick = onDiscardClick
            )
        }
    }
}

@Composable
private fun MealReviewEmptyCard(onDiscardClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No analysis to review.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onDiscardClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
private fun MealReviewResult(
    reviewedMeal: MealRecord,
    onConfirmClick: (MealRecord) -> Unit,
    onDiscardClick: () -> Unit
) {
    reviewedMeal.imageUri?.let { imageUri ->
        UriImage(
            imageUri = imageUri,
            placeholderText = "Unable to load meal photo",
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "AI Result",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = reviewedMeal.aiSummary ?: "AI analyzed your meal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${reviewedMeal.calories} kcal total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detected Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            reviewedMeal.foods.forEach { food ->
                ReviewFoodRow(food = food)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nutrition Total",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ReviewInfoRow("Carbs", "${reviewedMeal.carbsGram}g")
            ReviewInfoRow("Protein", "${reviewedMeal.proteinGram}g")
            ReviewInfoRow("Fat", "${reviewedMeal.fatGram}g")
            ReviewInfoRow("Fiber", "${reviewedMeal.fiberGram}g")
            ReviewInfoRow("Sugar", "${reviewedMeal.sugarGram}g")
            ReviewInfoRow("Sodium", "${reviewedMeal.sodiumMilligram}mg")
        }
    }

    ReviewActionButtons(
        primaryText = "Confirm Meal",
        secondaryText = "Discard",
        onPrimaryClick = { onConfirmClick(reviewedMeal) },
        onSecondaryClick = onDiscardClick
    )
}

@Composable
private fun ReviewFoodRow(food: MealFoodRecord) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Fiber ${food.fiberGram}g · Sugar ${food.sugarGram}g · Sodium ${food.sodiumMilligram}mg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReviewInfoRow(
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReviewActionButtons(
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onSecondaryClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(secondaryText)
        }
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier.weight(1f)
        ) {
            Text(primaryText)
        }
    }
}
