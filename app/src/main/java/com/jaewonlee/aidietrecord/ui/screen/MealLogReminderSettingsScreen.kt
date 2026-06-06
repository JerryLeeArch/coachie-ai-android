package com.jaewonlee.aidietrecord.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jaewonlee.aidietrecord.ui.theme.AppOutline
import com.jaewonlee.aidietrecord.ui.theme.AppSurface
import com.jaewonlee.aidietrecord.ui.theme.AppTextMuted

@Composable
fun MealLogReminderSettingsScreen(
    breakfastEnabled: Boolean,
    onBreakfastEnabledChange: (Boolean) -> Unit,
    lunchEnabled: Boolean,
    onLunchEnabledChange: (Boolean) -> Unit,
    dinnerEnabled: Boolean,
    onDinnerEnabledChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var pendingEnableChange by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingEnableChange?.invoke()
        }
        pendingEnableChange = null
    }
    val requestNotificationPermissionIfNeeded: (() -> Unit) -> Unit = { onGranted ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
        } else {
            pendingEnableChange = onGranted
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    val updateReminderEnabled: ((Boolean) -> Unit, Boolean) -> Unit = { onChange, enabled ->
        if (enabled) {
            requestNotificationPermissionIfNeeded { onChange(true) }
        } else {
            onChange(false)
        }
    }

    ScreenScaffold(
        title = "Meal Log Reminder Settings",
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
                text = "Choose the gentle check-ins you want during the day.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppTextMuted
            )
            ReminderToggleCard(
                title = "Breakfast",
                time = "8:00 AM",
                preview = "Start your morning with a quick meal log.",
                enabled = breakfastEnabled,
                onEnabledChange = { enabled ->
                    updateReminderEnabled(onBreakfastEnabledChange, enabled)
                }
            )
            ReminderToggleCard(
                title = "Lunch",
                time = "12:30 PM",
                preview = "A small lunch check-in keeps your day on track.",
                enabled = lunchEnabled,
                onEnabledChange = { enabled ->
                    updateReminderEnabled(onLunchEnabledChange, enabled)
                }
            )
            ReminderToggleCard(
                title = "Dinner",
                time = "6:30 PM",
                preview = "Wrap up gently with your dinner log.",
                enabled = dinnerEnabled,
                onEnabledChange = { enabled ->
                    updateReminderEnabled(onDinnerEnabledChange, enabled)
                }
            )
            Text(
                text = "We'll skip reminders when you've already logged a meal for that time.",
                style = MaterialTheme.typography.bodySmall,
                color = AppTextMuted
            )
        }
    }
}

@Composable
private fun ReminderToggleCard(
    title: String,
    time: String,
    preview: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, AppOutline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTextMuted
                )
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTextMuted
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}
