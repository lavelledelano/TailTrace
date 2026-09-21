package com.example.tailtrace

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.tailtrace.ui.theme.TailTraceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TailTraceTheme(dark = false) {
                Surface(Modifier.fillMaxSize().systemBarsPadding(), color = MaterialTheme.colorScheme.background) {
                    val ctx = LocalContext.current
                    AuthScreen(
                        onLogin = { email, _ ->
                            Toast.makeText(ctx, "Login looks valid: $email", Toast.LENGTH_SHORT).show()
                        },
                        onRegister = { name, _, _, _ ->
                            Toast.makeText(ctx, "Registration looks valid: $name", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
        }
    }
}