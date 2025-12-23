package nuist.cn.mymoment.view

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.MarkerOptions
import nuist.cn.mymoment.viewmodel.DiaryViewModel

@Composable
fun LocationPickerScreen(
    diaryViewModel: DiaryViewModel,
    onLocationSelected: () -> Unit
) {
    val context = LocalContext.current
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Use a disposable effect to manage the lifecycle of the MapView
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        ) { mapView ->
            mapView.onResume() // Ensure the map is resumed
            val aMap = mapView.map

            // Set initial camera position
            val nanjing = LatLng(32.06, 118.78)
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nanjing, 10f))

            // Handle map clicks
            aMap.setOnMapClickListener { latLng ->
                selectedLatLng = latLng
                diaryViewModel.onLocationChange(latLng)

                // Clear previous markers and add a new one
                aMap.clear()
                aMap.addMarker(MarkerOptions().position(latLng))
            }
        }

        Button(
            onClick = { onLocationSelected() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            enabled = selectedLatLng != null
        ) {
            Text("Confirm Location")
        }
    }

    // Manage lifecycle events
    DisposableEffect(Unit) {
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }
}
