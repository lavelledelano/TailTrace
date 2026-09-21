package com.example.tailtrace

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCaseScreen(vm: AppViewModel, nav: NavController) {
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Text("Report a missing pet", style = MaterialTheme.typography.titleLarge)
        }

        PhotoPicker(photo) { photo = it }

        OutlinedTextField(
            name, { name = it }, label = { Text("Pet name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Dog", "Cat", "Other").forEach {
                FilterChip(selected = species == it, onClick = { species = it }, label = { Text(it) })
            }
        }
        OutlinedTextField(
            breed, { breed = it }, label = { Text("Breed or appearance") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            color, { color = it }, label = { Text("Colour and markings") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            description, { description = it },
            label = { Text("Collar, tags, personality, anything that helps") },
            minLines = 3, modifier = Modifier.fillMaxWidth(),
        )

        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Last seen location", style = MaterialTheme.typography.titleMedium)
                    Text("%.4f, %.4f (your current location)".format(vm.lat, vm.lng))
                }
                TextButton(onClick = { requestLocation() }) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Update")
                }
            }
        }

        (formError ?: vm.error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                val bmp = photo
                formError = when {
                    bmp == null -> "Please add a photo so people can recognise your pet"
                    name.isBlank() || breed.isBlank() || color.isBlank() -> "Name, breed and colour are required"
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
            Text(if (vm.loading) "Posting..." else "Post missing pet")
        }
    }
}