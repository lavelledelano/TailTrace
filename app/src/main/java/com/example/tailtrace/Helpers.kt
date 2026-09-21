package com.example.tailtrace

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant

const val DEFAULT_LAT = -26.2041   // Johannesburg, used if location is denied
const val DEFAULT_LNG = 28.0473

fun timeAgo(iso: String): String {
    val mins = runCatching { Duration.between(Instant.parse(iso), Instant.now()).toMinutes() }.getOrDefault(0L)
    return when {
        mins < 1 -> "just now"
        mins < 60 -> "$mins min ago"
        mins < 1440 -> "${mins / 60} h ago"
        else -> "${mins / 1440} d ago"
    }
}

fun Bitmap.toBase64(maxSize: Int = 800): String {
    val scale = maxSize.toFloat() / maxOf(width, height)
    val bmp = if (scale < 1f) Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true) else this
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 75, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}

@SuppressLint("MissingPermission")
fun fetchLocation(ctx: Context, onResult: (Double, Double) -> Unit) {
    LocationServices.getFusedLocationProviderClient(ctx)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { l -> if (l != null) onResult(l.latitude, l.longitude) else onResult(DEFAULT_LAT, DEFAULT_LNG) }
        .addOnFailureListener { onResult(DEFAULT_LAT, DEFAULT_LNG) }
}

/** Returns a function that asks for permission (if needed) and fetches the location. If denied, the app still works. */
@Composable
fun rememberLocationRequester(onLocation: (Double, Double) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) fetchLocation(ctx, onLocation) else onLocation(DEFAULT_LAT, DEFAULT_LNG)
    }
    return {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) fetchLocation(ctx, onLocation)
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }
}

@Composable
fun PhotoPicker(bitmap: Bitmap?, onPicked: (Bitmap) -> Unit) {
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp -> bmp?.let(onPicked) }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            ctx.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inSampleSize = 2 })
            }?.let(onPicked)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bitmap?.let {
            Image(
                it.asImageBitmap(), contentDescription = "Selected photo",
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { camera.launch(null) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.PhotoCamera, null)
                Spacer(Modifier.width(6.dp))
                Text("Take photo")
            }
            OutlinedButton(
                onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.PhotoLibrary, null)
                Spacer(Modifier.width(6.dp))
                Text("Library")
            }
        }
    }
}