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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.model.MealFoodRecord
import com.jaewonlee.aidietrecord.data.model.MealRecord
import com.jaewonlee.aidietrecord.ui.util.formatMealDateTime

@Composable
fun MealDetailScreen(
    mealRecord: MealRecord?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    ScreenScaffold(
        title = "식단 상세",
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "식단 기록을 찾을 수 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
                return@Column
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFEAF1E8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (mealRecord.imageUri == null) {
                        "저장된 음식 이미지 없음"
                    } else {
                        "저장된 음식 이미지 표시 영역"
                    },
                    color = Color(0xFF52624F)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        text = "기록 시간: ${formatMealDateTime(mealRecord.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF52624F)
                    )
                    Text("${mealRecord.calories} kcal", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = mealRecord.memo.ifBlank { "메모 없음" },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("수정")
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "음식 목록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    mealRecord.foods.forEach { food ->
                        MealFoodInfoRow(food = food)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "영양분 구성",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    NutritionInfoRow("탄수화물", "${mealRecord.carbsGram}g")
                    NutritionInfoRow("단백질", "${mealRecord.proteinGram}g")
                    NutritionInfoRow("지방", "${mealRecord.fatGram}g")
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AI 분석 기록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("추정 음식명: ${mealRecord.aiFoodName ?: "없음"}")
                    Text("추정 칼로리: ${mealRecord.aiCalories ?: 0} kcal")
                    Text(
                        text = mealRecord.aiSummary ?: "AI 검수 기록 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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
                color = Color(0xFF2D6A4F)
            )
        }
        Text(
            text = "탄 ${food.carbsGram}g · 단 ${food.proteinGram}g · 지 ${food.fatGram}g",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF52624F)
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
            color = Color(0xFF2D6A4F)
        )
    }
}
