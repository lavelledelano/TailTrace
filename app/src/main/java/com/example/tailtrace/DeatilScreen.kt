package com.example.tailtrace

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: AppViewModel, nav: NavController, id: String) {
    val ctx = LocalContext.current
    LaunchedEffect(id) { vm.loadPet(id) }
    val pet = vm.selected?.takeIf { it.id == id }
    val me = vm.settings.collectAsState().value?.userId
    val base = ApiClient.BASE_URL.trimEnd('/')

    if (pet == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            if (vm.loading) CircularProgressIndicator() else Text(vm.error ?: "Case not found")
        }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            AsyncImage(
                model = base + pet.photoUrl, contentDescription = pet.petName,
                modifier = Modifier.fillMaxWidth().height(280.dp), contentScale = ContentScale.Crop,
            )
            IconButton(onClick = { nav.popBackStack() }, Modifier.padding(8.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(pet.petName, style = MaterialTheme.typography.headlineLarge)
            Text(
                "Status: " + pet.status.lowercase().replaceFirstChar { it.uppercase() },
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(pet.species + " • " + pet.breed + " • " + pet.color)
            Text("Last seen " + timeAgo(pet.lastSeenAt))
            if (!pet.description.isNullOrBlank()) Text(pet.description)

            Button(
                onClick = { nav.navigate("sighting/" + pet.id) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            ) {
                Icon(Icons.Default.Visibility, null)
                Spacer(Modifier.width(8.dp))
                Text("I saw this pet")
            }
            if (!pet.ownerPhone.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + pet.ownerPhone))) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Call " + (pet.ownerName ?: "owner"))
                }
            }

            if (pet.ownerId == me) {
                Text("Update case status", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ACTIVE", "PAUSED", "REUNITED", "CLOSED").forEach { s ->
                        FilterChip(
                            selected = pet.status == s,
                            onClick = { vm.setStatus(pet.id, s) },
                            label = { Text(s.lowercase().replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }

            Text("Sightings (" + vm.sightings.size + ")", style = MaterialTheme.typography.titleMedium)
            if (vm.sightings.isEmpty()) Text("No sightings yet.")
            vm.sightings.forEach { s ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (s.photoUrl != null) {
                            AsyncImage(
                                model = base + s.photoUrl, contentDescription = "Sighting photo",
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Column {
                            Text(timeAgo(s.seenAt), style = MaterialTheme.typography.titleMedium)
                            if (!s.direction.isNullOrBlank()) Text("Heading: " + s.direction)
                            if (!s.notes.isNullOrBlank()) Text(s.notes)
                        }
                    }
                }
            }
        }
    }
}