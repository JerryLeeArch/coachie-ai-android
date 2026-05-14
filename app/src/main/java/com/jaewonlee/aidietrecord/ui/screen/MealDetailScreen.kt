package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.jaewonlee.aidietrecord.data.sampleMeals
import com.jaewonlee.aidietrecord.ui.util.formatMealDateTime

@Composable
fun MealDetailScreen(
    mealId: Long,
    onBackClick: () -> Unit
) {
    val meal = sampleMeals.firstOrNull { it.id == mealId } ?: sampleMeals.first()

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color(0xFFEAF1E8), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("저장된 음식 이미지 표시 영역", color = Color(0xFF52624F))
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
                        text = meal.foodName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "기록 시간: ${formatMealDateTime(meal.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF52624F)
                    )
                    Text("${meal.calories} kcal", style = MaterialTheme.typography.titleMedium)
                    Text(meal.memo, style = MaterialTheme.typography.bodyMedium)
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
                    Text("추정 음식명: ${meal.aiFoodName ?: "없음"}")
                    Text("추정 칼로리: ${meal.aiCalories ?: 0} kcal")
                    Text(
                        text = "사용자가 AI 추정 결과를 확인하고 수정한 뒤 저장하는 흐름으로 확장할 예정",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
