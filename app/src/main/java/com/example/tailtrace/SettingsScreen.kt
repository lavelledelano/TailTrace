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
import androidx.compose.ui.res.stringResource
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
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        SectionTitle(stringResource(R.string.settings_profile))
        OutlinedTextField(
            name, { name = it }, label = { Text(stringResource(R.string.auth_name)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            phone, { phone = it }, label = { Text(stringResource(R.string.settings_contact)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        (formError ?: vm.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = {
                formError = when {
                    name.isBlank() -> ctx.getString(R.string.err_name)
                    !Validator.isValidPhone(phone) -> ctx.getString(R.string.err_phone)
                    else -> null
                }
                if (formError == null) {
                    vm.updateProfile(name.trim(), phone.trim()) {
                        Toast.makeText(ctx, ctx.getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = !vm.loading,
        ) { Text(stringResource(if (vm.loading) R.string.settings_saving else R.string.settings_save)) }

        HorizontalDivider()
        SectionTitle(stringResource(R.string.settings_language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("en" to "English", "af" to "Afrikaans", "zu" to "isiZulu").forEach { (code, label) ->
                FilterChip(
                    selected = s.lang == code,
                    onClick = { vm.setPref(Prefs.LANG, code) },
                    label = { Text(label) },
                )
            }
        }

        HorizontalDivider()
        SectionTitle(stringResource(R.string.settings_preferences))
        SettingSwitch(
            stringResource(R.string.settings_dark), stringResource(R.string.settings_dark_sub), s.dark,
        ) { vm.setPref(Prefs.DARK, it) }
        SettingSwitch(
            stringResource(R.string.settings_alerts), stringResource(R.string.settings_alerts_sub), s.notify,
        ) { vm.setPref(Prefs.NOTIFY, it) }

        Text(
            stringResource(R.string.settings_radius, radius.toInt()),
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = radius,
            onValueChange = { radius = it },
            onValueChangeFinished = { vm.setPref(Prefs.RADIUS, radius.toInt()) },
            valueRange = 1f..50f,
        )

        HorizontalDivider()
        SectionTitle(stringResource(R.string.settings_account))
        Text(stringResource(R.string.settings_password_note), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = { vm.logout() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.settings_logout))
        }
        Text(
            "TailTrace v1.0 • " + stringResource(R.string.auth_tagline),
            style = MaterialTheme.typography.bodyMedium,
        )
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