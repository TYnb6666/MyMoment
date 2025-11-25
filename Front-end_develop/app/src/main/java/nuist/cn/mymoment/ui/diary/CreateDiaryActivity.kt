package nuist.cn.mymoment.ui.diary

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import nuist.cn.mymoment.R
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.repository.DiaryRepository
import nuist.cn.mymoment.util.LocationHelper

class CreateDiaryActivity : AppCompatActivity() {

    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText
    private lateinit var temperatureEditText: TextInputEditText
    private lateinit var weatherAutoComplete: MaterialAutoCompleteTextView
    private lateinit var locationValue: TextView
    private lateinit var saveButton: MaterialButton
    private lateinit var locationButton: MaterialButton
    private val diaryRepository = DiaryRepository
    private lateinit var locationHelper: LocationHelper
    private var currentDiary: Diary? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                requestLocation()
            } else {
                Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diary_editor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationHelper = LocationHelper(this)
        currentDiary = intent.getParcelableExtra(EXTRA_DIARY)
        bindViews()
        populateDiaryIfNeeded()
    }

    private fun bindViews() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        titleEditText = findViewById(R.id.titleEditText)
        contentEditText = findViewById(R.id.contentEditText)
        temperatureEditText = findViewById(R.id.temperatureEditText)
        weatherAutoComplete = findViewById(R.id.weatherAutoComplete)
        locationValue = findViewById(R.id.locationValue)
        saveButton = findViewById(R.id.saveButton)
        locationButton = findViewById(R.id.locationButton)

        val weatherAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.weather_options)
        )
        weatherAutoComplete.setAdapter(weatherAdapter)

        locationButton.setOnClickListener { ensureLocationPermission() }

        saveButton.setOnClickListener { saveDiary() }
    }

    private fun populateDiaryIfNeeded() {
        val diary = currentDiary
        if (diary == null) {
            saveButton.setText(R.string.save_diary)
            findViewById<MaterialToolbar>(R.id.toolbar).title = getString(R.string.create_diary_title)
            return
        }
        findViewById<MaterialToolbar>(R.id.toolbar).title = getString(R.string.edit_diary_title)
        saveButton.setText(R.string.update_diary)
        titleEditText.setText(diary.title)
        contentEditText.setText(diary.content)
        weatherAutoComplete.setText(diary.weather, false)
        temperatureEditText.setText(diary.temperature)
        locationValue.text = if (diary.locationName.isNotBlank()) diary.locationName else getString(R.string.diary_location_unknown)
        latitude = diary.latitude
        longitude = diary.longitude
    }

    private fun ensureLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            requestLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun requestLocation() {
        locationHelper.requestSingleUpdate { location, error ->
            if (error != null) {
                Toast.makeText(this, getString(R.string.location_fetch_failed, error), Toast.LENGTH_SHORT).show()
                return@requestSingleUpdate
            }
            location?.let {
                latitude = it.latitude
                longitude = it.longitude
                locationValue.text = it.address ?: "${it.latitude}, ${it.longitude}"
            }
        }
    }

    private fun saveDiary() {
        val title = titleEditText.text?.toString()?.trim().orEmpty()
        val content = contentEditText.text?.toString()?.trim().orEmpty()
        if (title.isBlank()) {
            titleEditText.error = getString(R.string.title_required)
            titleEditText.requestFocus()
            return
        }
        if (content.isBlank()) {
            contentEditText.error = getString(R.string.content_required)
            contentEditText.requestFocus()
            return
        }
        val newDiary = Diary(
            id = currentDiary?.id.orEmpty(),
            title = title,
            content = content,
            weather = weatherAutoComplete.text?.toString().orEmpty(),
            temperature = temperatureEditText.text?.toString().orEmpty(),
            locationName = locationValue.text?.toString().orEmpty(),
            latitude = latitude,
            longitude = longitude
        )
        if (currentDiary == null) {
            diaryRepository.addDiary(newDiary)
            Toast.makeText(this, R.string.diary_saved_success, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val success = diaryRepository.updateDiary(newDiary.copy(id = currentDiary!!.id))
            if (success) {
                Toast.makeText(this, R.string.diary_update_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.diary_update_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stop()
        locationHelper.release()
    }

    companion object {
        const val EXTRA_DIARY = "extra_diary"
    }
}

