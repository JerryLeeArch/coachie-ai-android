package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var userId by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var localErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var hideRemoteError by rememberSaveable { mutableStateOf(false) }
    val visibleErrorMessage = localErrorMessage ?: errorMessage.takeUnless { hideRemoteError }

    ScreenScaffold(title = "AI 식단 기록 로그인") { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "회원가입" else "로그인",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRegisterMode) {
                            "새 계정을 만든 뒤 바로 앱을 사용할 수 있습니다."
                        } else {
                            "아이디와 비밀번호를 입력해 주세요."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = userId,
                        onValueChange = {
                            userId = it
                            localErrorMessage = null
                            hideRemoteError = true
                        },
                        label = { Text("아이디") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = {
                                nickname = it
                                localErrorMessage = null
                                hideRemoteError = true
                            },
                            label = { Text("닉네임") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localErrorMessage = null
                            hideRemoteError = true
                        },
                        label = { Text("비밀번호") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (visibleErrorMessage != null) {
                        Text(
                            text = visibleErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = {
                            localErrorMessage = validateAuthInput(
                                userId = userId,
                                nickname = nickname,
                                password = password,
                                isRegisterMode = isRegisterMode
                            )
                            if (localErrorMessage == null) {
                                hideRemoteError = false
                                if (isRegisterMode) {
                                    onRegisterClick(userId, nickname, password)
                                } else {
                                    onLoginClick(userId, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegisterMode) "회원가입 후 시작" else "로그인")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                localErrorMessage = null
                                hideRemoteError = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isRegisterMode) "로그인으로" else "회원가입")
                        }
                    }
                }
            }
        }
    }
}

private fun validateAuthInput(
    userId: String,
    nickname: String,
    password: String,
    isRegisterMode: Boolean
): String? {
    return when {
        userId.isBlank() -> "아이디를 입력해 주세요."
        isRegisterMode && nickname.isBlank() -> "닉네임을 입력해 주세요."
        password.isBlank() -> "비밀번호를 입력해 주세요."
        password.length < 4 -> "비밀번호는 4자 이상 입력해 주세요."
        else -> null
    }
}
