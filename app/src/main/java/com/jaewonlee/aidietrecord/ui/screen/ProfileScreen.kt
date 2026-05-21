package com.jaewonlee.aidietrecord.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    userId: String,
    onUserIdChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ScreenScaffold(
        title = "프로필",
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
                text = "닉네임, 아이디, 비밀번호를 수정합니다. 비밀번호는 비워두면 기존 값이 유지됩니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("닉네임") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("아이디") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("새 비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("프로필 저장")
            }
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("로그아웃")
            }
        }
    }
}
