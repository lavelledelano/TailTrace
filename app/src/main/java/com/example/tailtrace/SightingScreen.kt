package com.example.tailtrace

import android.graphics.Bitmap
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightingScreen(vm: AppViewModel, nav: NavController, petId: String) {
    var direction by remember { mutableStateOf("Unknown") }
    var notes by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<Bitmap?>(null) }
    val requestLocation = rememberLocationRequester { la, ln -> vm.setLocation(la, ln) }
    val directions = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "Unknown")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
            Column {
                Text("Report a sighting", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Help " + (vm.selected?.petName ?: "this pet") + " get home safely",
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Text("Add a photo (optional)", style = MaterialTheme.typography.titleMedium)
        PhotoPicker(photo) { photo = it }

        Text("Sighting location", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("%.4f, %.4f".format(vm.lat, vm.lng), Modifier.weight(1f))
                TextButton(onClick = { requestLocation() }) {
                    Icon(Icons.Default.MyLocation, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Use current location")
                }
            }
        }

        Text("Direction it was heading", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            directions.forEach {
                FilterChip(selected = direction == it, onClick = { direction = it }, label = { Text(it) })
            }
        }

        OutlinedTextField(
            notes, { if (it.length <= 200) notes = it },
            label = { Text("What did you notice?") },
            supportingText = { Text(notes.length.toString() + "/200") },
            minLines = 3, modifier = Modifier.fillMaxWidth(),
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(10.dp))
                Text("Please observe from a safe distance. Don't chase or corner the animal. Keep yourself and the pet safe.")
            }
        }

        vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = {
                vm.addSighting(
                    NewSighting(petId, vm.lat, vm.lng, direction, notes.trim(), photo?.toBase64())
                ) {
                    vm.loadPet(petId)
                    nav.popBackStack()
                }
            },
            enabled = !vm.loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
        ) {
            Text(if (vm.loading) "Sharing..." else "Share sighting")
        }
    }
}