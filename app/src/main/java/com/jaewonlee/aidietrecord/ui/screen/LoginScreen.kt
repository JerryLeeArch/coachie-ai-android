package com.jaewonlee.aidietrecord.ui.screen

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSurface

@Composable
fun LoginScreen(
    errorMessage: String?,
    infoMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    onPasswordResetClick: (String) -> Unit
) {
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var userId by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var localErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var hideRemoteMessage by rememberSaveable { mutableStateOf(false) }
    val visibleErrorMessage = localErrorMessage ?: errorMessage.takeUnless { hideRemoteMessage }
    val visibleInfoMessage = infoMessage
        .takeUnless { hideRemoteMessage }
        ?.takeIf { visibleErrorMessage == null }

    ScreenScaffold(title = "AI Meal Log") { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AppSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, AppOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Log In",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRegisterMode) {
                            "Create a local account and start tracking meals."
                        } else {
                            "Enter your email and password."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = userId,
                        onValueChange = {
                            userId = it
                            localErrorMessage = null
                            hideRemoteMessage = true
                        },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = {
                                nickname = it
                                localErrorMessage = null
                                hideRemoteMessage = true
                            },
                            label = { Text("Nickname") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localErrorMessage = null
                            hideRemoteMessage = true
                        },
                        label = { Text("Password") },
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
                    if (visibleInfoMessage != null) {
                        Text(
                            text = visibleInfoMessage,
                            color = MaterialTheme.colorScheme.primary,
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
                                hideRemoteMessage = false
                                if (isRegisterMode) {
                                    onRegisterClick(userId, nickname, password)
                                } else {
                                    onLoginClick(userId, password)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRegisterMode) "Create Account" else "Log In")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                localErrorMessage = null
                                hideRemoteMessage = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isRegisterMode) "Back to Log In" else "Create Account")
                        }
                    }
                    if (!isRegisterMode) {
                        TextButton(
                            onClick = {
                                localErrorMessage = validatePasswordResetInput(userId)
                                if (localErrorMessage == null) {
                                    hideRemoteMessage = false
                                    onPasswordResetClick(userId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Forgot Password?")
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
        userId.isBlank() -> "Enter an email."
        !userId.isValidEmail() -> "Enter a valid email."
        isRegisterMode && nickname.isBlank() -> "Enter a nickname."
        password.isBlank() -> "Enter a password."
        password.length < 4 -> "Password must be at least 4 characters."
        else -> null
    }
}

private fun validatePasswordResetInput(userId: String): String? {
    return when {
        userId.isBlank() -> "Enter an email."
        !userId.isValidEmail() -> "Enter a valid email."
        else -> null
    }
}

private fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(trim()).matches()
}
