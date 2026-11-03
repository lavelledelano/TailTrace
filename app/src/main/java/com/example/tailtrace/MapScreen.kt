package com.example.tailtrace

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import androidx.compose.ui.res.stringResource
@Composable
fun MapScreen(vm: AppViewModel, nav: NavController) {
    val ctx = LocalContext.current
    remember {
        val cfg = Configuration.getInstance()
        cfg.load(ctx, ctx.getSharedPreferences("osm", Context.MODE_PRIVATE))
        cfg.userAgentValue = "TailTrace-StudentProject/1.0 (github.com/lavelledelano/TailTrace)"
        cfg.osmdroidBasePath = File(ctx.cacheDir, "osmdroid_v4")
        cfg.osmdroidTileCache = File(ctx.cacheDir, "osmdroid_v4/tiles")
        true
    }
    var preview by remember { mutableStateOf<Pet?>(null) }
    val mapView = remember {
        MapView(ctx).apply {
            setTileSource(
                object : OnlineTileSourceBase(
                    "EsriStreet", 0, 19, 256, "",
                    arrayOf("https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"),
                    "Tiles © Esri",
                ) {
                    override fun getTileURLString(pMapTileIndex: Long): String {
                        return baseUrl +
                                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                                MapTileIndex.getY(pMapTileIndex) + "/" +
                                MapTileIndex.getX(pMapTileIndex)
                    }
                }
            )
            setMultiTouchControls(true)
            controller.setZoom(14.0)
        }
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { map ->
                map.controller.setCenter(GeoPoint(vm.lat, vm.lng))
                map.overlays.clear()
                vm.pets.forEach { pet ->
                    val marker = Marker(map)
                    marker.position = GeoPoint(pet.lat, pet.lng)
                    marker.title = pet.petName
                    marker.setOnMarkerClickListener { _, _ ->
                        preview = pet
                        true
                    }
                    map.overlays.add(marker)
                }
                map.invalidate()
            },
        )

        Surface(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            shape = MaterialTheme.shapes.large,
            shadowElevation = 4.dp,
        ) {
            Text(
                stringResource(R.string.map_count, vm.pets.size),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        ExtendedFloatingActionButton(
            onClick = { nav.navigate("create") },
            icon = { Icon(Icons.Default.Add, null) },
            text = { Text(stringResource(R.string.report_missing)) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        )

        preview?.let { pet ->
            Box(Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
                PetCard(pet) { nav.navigate("detail/" + pet.id) }
            }
        }
    }
}