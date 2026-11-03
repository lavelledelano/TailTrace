package com.example.tailtrace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    loading: Boolean = false,
    serverError: String? = null,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (name: String, email: String, password: String, phone: String) -> Unit,
) {
    val ctx = LocalContext.current
    var isRegister by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        formError = when {
            isRegister && name.isBlank() -> ctx.getString(R.string.err_name)
            !Validator.isValidEmail(email) -> ctx.getString(R.string.err_email)
            password.isBlank() -> ctx.getString(R.string.err_password_required)
            isRegister && !Validator.isStrongPassword(password) -> ctx.getString(R.string.err_password_weak)
            isRegister && !Validator.isValidPhone(phone) -> ctx.getString(R.string.err_phone)
            else -> null
        }
        if (formError != null) return
        if (isRegister) onRegister(name.trim(), email.trim(), password, phone.trim())
        else onLogin(email.trim(), password)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Pets, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Text("TailTrace", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Text(stringResource(R.string.auth_tagline), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))

        if (isRegister) {
            OutlinedTextField(
                name, { name = it }, label = { Text(stringResource(R.string.auth_name)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                phone, { phone = it }, label = { Text(stringResource(R.string.auth_phone)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedTextField(
            email, { email = it }, label = { Text(stringResource(R.string.auth_email)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            password, { password = it }, label = { Text(stringResource(R.string.auth_password)) }, singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        stringResource(R.string.auth_toggle_password),
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        (formError ?: serverError)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { submit() }, enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            else Text(stringResource(if (isRegister) R.string.auth_create_account else R.string.auth_sign_in))
        }
        TextButton(onClick = { isRegister = !isRegister; formError = null }) {
            Text(stringResource(if (isRegister) R.string.auth_have_account else R.string.auth_new_here))
        }
    }
}