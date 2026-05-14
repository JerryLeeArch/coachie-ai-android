package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.data.sampleMeals

@Composable
fun HomeScreen(
    onAddMealClick: () -> Unit,
    onMealListClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val totalCalories = sampleMeals.sumOf { it.calories }
    val targetCalories = 2000
    val progress = (totalCalories / targetCalories.toFloat()).coerceIn(0f, 1f)

    ScreenScaffold(title = "AI 식단 기록") { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "오늘의 식단 요약",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "음식 기록과 AI 분석 결과를 한 곳에서 관리합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("섭취 칼로리", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$totalCalories kcal / $targetCalories kcal",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = Color(0xFFE0E7DE)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryChip(label = "기록", value = "${sampleMeals.size}개")
                        SummaryChip(label = "남은 칼로리", value = "${targetCalories - totalCalories} kcal")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddMealClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("음식 추가")
                }
                OutlinedButton(
                    onClick = onMealListClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("기록 보기")
                }
            }

            OutlinedButton(
                onClick = onProfileClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("내 정보 설정")
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
                        text = "11주차 구현 체크",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CheckRow("5개 화면 구성")
                    CheckRow("Navigation Compose 화면 전환")
                    CheckRow("LazyColumn 리스트 화면")
                    CheckRow("Room DB Entity / DAO / Database 설계")
                    CheckRow("이미지 미리보기 영역 구성")
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(label: String, value: String) {
    Column(
        modifier = Modifier
            .background(Color(0xFFEFF6EE), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF52624F))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CheckRow(text: String) {
    Row {
        Text("완료", color = Color(0xFF2D6A4F), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
