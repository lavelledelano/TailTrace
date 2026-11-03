package com.example.tailtrace

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCaseScreen(vm: AppViewModel, nav: NavController) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Dog") }
    var breed by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }
    val requestLocation = rememberLocationRequester { la, ln -> vm.setLocation(la, ln) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
            }
            Text(stringResource(R.string.create_title), style = MaterialTheme.typography.titleLarge)
        }

        PhotoPicker(photo) { photo = it }

        OutlinedTextField(
            name, { name = it }, label = { Text(stringResource(R.string.create_name)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Dog", "Cat", "Other").forEach {
                FilterChip(
                    selected = species == it,
                    onClick = { species = it },
                    label = { Text(speciesLabel(ctx, it)) },
                )
            }
        }
        OutlinedTextField(
            breed, { breed = it }, label = { Text(stringResource(R.string.create_breed)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            color, { color = it }, label = { Text(stringResource(R.string.create_color)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            description, { description = it },
            label = { Text(stringResource(R.string.create_desc)) },
            minLines = 3, modifier = Modifier.fillMaxWidth(),
        )

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.create_location), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.create_location_sub, "%.4f, %.4f".format(vm.lat, vm.lng)))
                }
                TextButton(onClick = { requestLocation() }) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.create_update))
                }
            }
        }

        (formError ?: vm.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val bmp = photo
                formError = when {
                    bmp == null -> ctx.getString(R.string.err_photo)
                    name.isBlank() || breed.isBlank() || color.isBlank() -> ctx.getString(R.string.err_required)
                    else -> null
                }
                if (formError == null && bmp != null) {
                    vm.createPet(
                        NewPet(
                            name.trim(), species, breed.trim(), color.trim(), description.trim(),
                            bmp.toBase64(), vm.lat, vm.lng, Instant.now().toString(),
                        )
                    ) {
                        vm.loadNearby()
                        nav.popBackStack()
                    }
                }
            },
            enabled = !vm.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(stringResource(if (vm.loading) R.string.create_posting else R.string.create_post))
        }
    }
}