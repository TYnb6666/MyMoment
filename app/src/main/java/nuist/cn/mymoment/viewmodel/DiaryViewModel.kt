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
    val editingId: String? = null,
    val title: String = "",
    val content: String = "",
    val location: GeoPoint? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveComplete: Boolean = false
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
                location = state.location,
                timestamp = System.currentTimeMillis()
            )

            val result = if (state.editingId == null) {
                repository.addDiary(diary)
            } else {
                repository.updateDiary(state.editingId, diary)
            }

            if (result.isSuccess) {
                editState.value = state.copy(isSaving = false, saveComplete = true)
            } else {
                editState.value = state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun startEdit(diary: Diary) {
        editState.value = DiaryEditUiState(
            editingId = diary.id,
            title = diary.title,
            content = diary.content,
            location = diary.location
        )
    }

    fun deleteDiary(diaryId: String) {
        viewModelScope.launch {
            val result = repository.deleteDiary(diaryId)
            if (result.isFailure) {
                errorState.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun prepareNewDiary() {
        editState.value = DiaryEditUiState()
    }
    
    // --- 搜索逻辑开始 ---
    var searchQuery = mutableStateOf("") // 搜索关键词
        private set

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    // 原始日记列表
    private var allDiaries = mutableStateOf<List<Diary>>(emptyList())
    
    // 过滤后的日记列表（供 UI 使用）
    var diaryListState = mutableStateOf<List<Diary>>(emptyList())
        private set

    private fun updateFilteredList() {
        val query = searchQuery.value.lowercase().trim()
        diaryListState.value = if (query.isEmpty()) {
            allDiaries.value
        } else {
            allDiaries.value.filter {
                it.title.lowercase().contains(query) || it.content.lowercase().contains(query)
            }
        }
    }
    // --- 搜索逻辑结束 ---

    var errorState = mutableStateOf<String?>(null)
        private set

    private var alreadyObserving = false

    fun startObserveDiaries() {
        if (alreadyObserving) return
        alreadyObserving = true

        repository.observeDiaries(
            onUpdate = { list -> 
                allDiaries.value = list
                updateFilteredList() // 更新列表
            },
            onError = { e -> errorState.value = e.message }
        )
    }

    fun stopObserveDiaries() {
        repository.stopObserving()
        alreadyObserving = false
    }

    // 当搜索词改变时，手动触发过滤
    fun performSearch() {
        updateFilteredList()
    }
}