package com.jaewonlee.aidietrecord.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jaewonlee.aidietrecord.R

@Composable
fun ProfileScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    userId: String,
    onUserIdChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    infoMessage: String?,
    darkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    isDataTransferInProgress: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onMealLogReminderSettingsClick: () -> Unit,
    onPasswordResetClick: () -> Unit,
    onExportDataSelected: (Uri) -> Unit,
    onImportDataSelected: (Uri) -> Unit,
    onLogoutClick: () -> Unit
) {
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportDataSelected(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportDataSelected(uri)
        }
    }

    ScreenScaffold(
        title = "Profile",
        onBackClick = onBackClick,
        actions = {
            DarkModeAction(
                darkThemeEnabled = darkThemeEnabled,
                onDarkThemeChange = onDarkThemeChange
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Update your nickname, email, or password. Leave the password blank to keep the current one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                label = { Text("Nickname") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("New Password") },
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
            if (infoMessage != null) {
                Text(
                    text = infoMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Profile")
            }
            OutlinedButton(
                onClick = onMealLogReminderSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                ButtonLabelWithIcon(
                    text = "Meal Log Reminder Settings",
                    iconResId = R.drawable.ic_notification_20
                )
            }
            OutlinedButton(
                onClick = onPasswordResetClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Password Reset Email")
            }
            OutlinedButton(
                onClick = { exportLauncher.launch("coachie-ai-user-data.json") },
                enabled = !isDataTransferInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDataTransferInProgress) {
                    Text("Working...")
                } else {
                    ButtonLabelWithIcon(
                        text = "Export User Data",
                        iconResId = R.drawable.ic_export_20
                    )
                }
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "*/*")) },
                enabled = !isDataTransferInProgress,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDataTransferInProgress) {
                    Text("Working...")
                } else {
                    ButtonLabelWithIcon(
                        text = "Import User Data",
                        iconResId = R.drawable.ic_import_20
                    )
                }
            }
            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }
}

@Composable
private fun DarkModeAction(
    darkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dark",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = darkThemeEnabled,
            onCheckedChange = onDarkThemeChange
        )
    }
}

@Composable
private fun ButtonLabelWithIcon(
    text: String,
    iconResId: Int
) {
    Text(text)
    Spacer(modifier = Modifier.width(8.dp))
    Icon(
        painter = painterResource(iconResId),
        contentDescription = null,
        modifier = Modifier.size(18.dp)
    )
}
