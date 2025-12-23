package nuist.cn.mymoment.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.LatLng
import com.amap.api.maps2d.model.LatLngBounds
import com.amap.api.maps2d.model.MarkerOptions
import nuist.cn.mymoment.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEntriesMapScreen(
    diaryViewModel: DiaryViewModel,
    onBack: () -> Unit
) {
    val diaries by diaryViewModel.diaryListState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary Footprints") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        onCreate(null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { mapView ->
                mapView.onResume()
                val aMap = mapView.map
                aMap.clear() // Clear previous markers

                val locations = diaries.mapNotNull { it.location }
                if (locations.isNotEmpty()) {
                    val boundsBuilder = LatLngBounds.Builder()
                    locations.forEach { geoPoint ->
                        val latLng = LatLng(geoPoint.latitude, geoPoint.longitude)
                        aMap.addMarker(MarkerOptions().position(latLng))
                        boundsBuilder.include(latLng)
                    }
                    // Move camera to show all markers
                    val bounds = boundsBuilder.build()
                    aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)) // 150 is padding in pixels
                } else {
                    // If no locations, center on a default position (e.g., Nanjing)
                    val nanjing = LatLng(32.06, 118.78)
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nanjing, 10f))
                }
            }
        }
    }
}
