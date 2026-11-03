package com.example.tailtrace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasesScreen(vm: AppViewModel, nav: NavController) {
    val ctx = LocalContext.current
    var query by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("All") }
    val radius = vm.settings.collectAsState().value?.radius ?: 10
    val shown = vm.pets.filter {
        (species == "All" || it.species == species) &&
                (query.isBlank() || it.petName.contains(query, true) || it.breed.contains(query, true))
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.cases_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.cases_sub), color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { vm.loadNearby() }) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                }
            }
            OutlinedTextField(
                query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.cases_search)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Dog", "Cat", "Other").forEach {
                    FilterChip(
                        selected = species == it,
                        onClick = { species = it },
                        label = { Text(speciesLabel(ctx, it)) },
                    )
                }
            }
            if (vm.loading && vm.pets.isEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (shown.isEmpty() && !vm.loading) {
                Text(stringResource(R.string.cases_empty, radius))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(shown, key = { it.id }) { pet ->
                    PetCard(pet) { nav.navigate("detail/" + pet.id) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { nav.navigate("create") },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text(stringResource(R.string.report_missing)) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
fun PetCard(pet: Pet, onClick: () -> Unit) {
    val ctx = LocalContext.current
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ApiClient.BASE_URL.trimEnd('/') + pet.photoUrl,
                contentDescription = pet.petName,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(pet.petName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(speciesLabel(ctx, pet.species) + " • " + pet.breed, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.last_seen, timeAgoL(ctx, pet.lastSeenAt)),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}