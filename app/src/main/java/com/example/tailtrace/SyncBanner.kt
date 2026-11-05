package com.example.tailtrace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SyncBanner(vm: AppViewModel) {
    if (vm.online && vm.pending == 0) return
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (!vm.online) {
                Text(stringResource(R.string.offline_banner), style = MaterialTheme.typography.bodyMedium)
            }
            if (vm.pending > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.pending_banner, vm.pending),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.syncNow() }, enabled = !vm.syncing) {
                        Text(stringResource(R.string.sync_now))
                    }
                }
            }
        }
    }
}