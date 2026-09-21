package com.example.tailtrace

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val s = vm.settings.collectAsState().value ?: return
    val ctx = LocalContext.current
    var name by remember(s.name) { mutableStateOf(s.name) }
    var phone by remember(s.phone) { mutableStateOf(s.phone) }
    var radius by remember(s.radius) { mutableStateOf(s.radius.toFloat()) }
    var formError by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        SectionTitle("Profile")
        OutlinedTextField(
            name, { name = it }, label = { Text("Full name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            phone, { phone = it }, label = { Text("Preferred contact number") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        (formError ?: vm.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                formError = when {
                    name.isBlank() -> "Name can't be empty"
                    !Validator.isValidPhone(phone) -> "Enter a valid phone number, e.g. 0821234567"
                    else -> null
                }
                if (formError == null) {
                    vm.updateProfile(name.trim(), phone.trim()) {
                        Toast.makeText(ctx, "Profile saved", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !vm.loading,
        ) { Text(if (vm.loading) "Saving..." else "Save profile") }

        HorizontalDivider()
        SectionTitle("Preferences")
        SettingSwitch("Dark mode", "Easier on the eyes at night", s.dark) { vm.setPref(Prefs.DARK, it) }
        SettingSwitch("Nearby alerts", "Get notified about new lost pets in your area", s.notify) {
            vm.setPref(Prefs.NOTIFY, it)
        }

        Text("Search radius: " + radius.toInt() + " km", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = radius,
            onValueChange = { radius = it },
            onValueChangeFinished = { vm.setPref(Prefs.RADIUS, radius.toInt()) },
            valueRange = 1f..50f,
        )

        HorizontalDivider()
        SectionTitle("Account")
        Text(
            "Your password is never stored on this phone. The server keeps only a secure hash of it.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Log out")
        }
        Text("TailTrace v1.0 • Tracking every pawprint", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}