package nuist.cn.mymoment.ui.map

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.google.android.material.appbar.MaterialToolbar
import nuist.cn.mymoment.R
import nuist.cn.mymoment.repository.DiaryRepository

class DiaryMapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var amap: AMap
    private val repository = DiaryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        amap = mapView.map

        val toolbar: MaterialToolbar = findViewById(R.id.mapToolbar)
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { finish() }

        loadMarkers()
    }

    private fun loadMarkers() {
        val markers = repository.getAll().filter { it.latitude != null && it.longitude != null }
        if (markers.isEmpty()) {
            Toast.makeText(this, R.string.map_no_markers, Toast.LENGTH_SHORT).show()
            return
        }
        markers.forEach { diary ->
            val position = LatLng(diary.latitude!!, diary.longitude!!)
            amap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(diary.title)
                    .snippet(diary.locationName)
            )
        }
        val first = markers.first()
        amap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(first.latitude!!, first.longitude!!),
                12f
            )
        )
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}

