package com.example.tailtrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tailtrace.ui.theme.TailTraceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TailTraceApp() }
    }
}

@Composable
fun TailTraceApp(vm: AppViewModel = viewModel()) {
    val s = vm.settings.collectAsState().value
    TailTraceTheme(dark = s?.dark ?: false) {
        Surface(Modifier.fillMaxSize().systemBarsPadding(), color = MaterialTheme.colorScheme.background) {
            when {
                s == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                s.token == null -> AuthScreen(
                    loading = vm.loading,
                    serverError = vm.error,
                    onLogin = { email, password -> vm.login(email, password) },
                    onRegister = { name, email, password, phone -> vm.register(name, email, password, phone) },
                )
                else -> HomePlaceholder(s.name) { vm.logout() }
            }
        }
    }
}

@Composable
fun HomePlaceholder(name: String, onLogout: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Welcome, " + name, style = MaterialTheme.typography.titleLarge)
        Text("You are signed in.")
        Button(onClick = onLogout) { Text("Log out") }
    }
}