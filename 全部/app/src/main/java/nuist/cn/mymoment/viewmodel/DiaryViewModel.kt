package nuist.cn.mymoment.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps2d.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.repository.DiaryRepository


data class DiaryEditUiState(
    val title: String = "",
    val content: String = "",
    val location: GeoPoint? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveComplete: Boolean = false // New state to signal completion
)

class DiaryViewModel(
    private val repository: DiaryRepository = DiaryRepository()
) : ViewModel() {

    var editState = mutableStateOf(DiaryEditUiState())
        private set

    fun onTitleChange(newTitle: String) {
        editState.value = editState.value.copy(title = newTitle)
    }

    fun onContentChange(newContent: String) {
        editState.value = editState.value.copy(content = newContent)
    }

    fun onLocationChange(latLng: LatLng) {
        editState.value = editState.value.copy(
            location = GeoPoint(latLng.latitude, latLng.longitude)
        )
    }

    // ViewModel now no longer takes a callback, decoupling it from navigation.
    fun saveDiary() {
        if (editState.value.isSaving) return

        val state = editState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            editState.value = state.copy(error = "Title or content cannot be empty")
            return
        }

        viewModelScope.launch {
            editState.value = state.copy(isSaving = true, error = null)

            val diary = Diary(
                title = state.title,
                content = state.content,
                location = state.location
            )

            val result = repository.addDiary(diary)

            editState.value = if (result.isSuccess) {
                // On success, update the state to signal completion.
                // The UI will observe this and navigate.
                state.copy(isSaving = false, saveComplete = true)
            } else {
                state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // Call this to reset the state before navigating to the edit screen.
    fun prepareNewDiary() {
        editState.value = DiaryEditUiState()
    }
    
    var diaryListState = mutableStateOf<List<Diary>>(emptyList())
        private set

    var errorState = mutableStateOf<String?>(null)
        private set

    private var alreadyObserving = false

    fun startObserveDiaries() {
        if (alreadyObserving) return
        alreadyObserving = true

        repository.observeDiaries(
            onUpdate = { list -> diaryListState.value = list },
            onError = { e -> errorState.value = e.message }
        )
    }

    fun stopObserveDiaries() {
        repository.stopObserving()
        alreadyObserving = false
    }
}