package nuist.cn.mymoment.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps2d.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import nuist.cn.mymoment.model.Diary
import nuist.cn.mymoment.repository.AuthRepository
import nuist.cn.mymoment.repository.DiaryRepository


/**
 * UI state for the diary editing screen.
 */
data class DiaryEditUiState(
    val editingId: String? = null,
    val title: String = "",
    val content: String = "",
    val location: GeoPoint? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveComplete: Boolean = false
)

/**
 * ViewModel for managing diary data and business logic.
 * It handles fetching, creating, updating, deleting, and searching diaries,
 * and reacts to authentication state changes.
 */
class DiaryViewModel(
    private val diaryRepository: DiaryRepository = DiaryRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // Holds the state for the diary editing screen.
    var editState = mutableStateOf(DiaryEditUiState())
        private set

    init {
        // Observe authentication state changes to automatically load/clear data.
        viewModelScope.launch {
            authRepository.getAuthState().collect { user ->
                if (user != null) {
                    // User is logged in, start observing their diaries.
                    startObserveDiaries()
                } else {
                    // User is logged out, stop observing and clear all data.
                    stopObserveDiaries()
                    allDiaries.value = emptyList()
                    updateFilteredList() // This will clear diaryListState.
                    errorState.value = null
                }
            }
        }
    }

    // Updates the title in the edit state.
    fun onTitleChange(newTitle: String) {
        editState.value = editState.value.copy(title = newTitle)
    }

    // Updates the content in the edit state.
    fun onContentChange(newContent: String) {
        editState.value = editState.value.copy(content = newContent)
    }

    // Updates the location in the edit state.
    fun onLocationChange(latLng: LatLng) {
        editState.value = editState.value.copy(
            location = GeoPoint(latLng.latitude, latLng.longitude)
        )
    }

    /**
     * Saves a new diary or updates an existing one.
     */
    fun saveDiary() {
        // Prevent multiple save operations if already saving.
        if (editState.value.isSaving) return

        val state = editState.value
        // Basic validation: title or content must not be blank.
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

            // Decide whether to add a new diary or update an existing one.
            val result = if (state.editingId == null) {
                diaryRepository.addDiary(diary)
            } else {
                diaryRepository.updateDiary(state.editingId, diary)
            }

            if (result.isSuccess) {
                // Indicate that save is complete to trigger navigation or UI changes.
                editState.value = state.copy(isSaving = false, saveComplete = true)
            } else {
                // Set error message on failure.
                editState.value = state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Prepares the ViewModel to edit an existing diary by populating the edit state.
     * @param diary The diary to be edited.
     */
    fun startEdit(diary: Diary) {
        editState.value = DiaryEditUiState(
            editingId = diary.id,
            title = diary.title,
            content = diary.content,
            location = diary.location
        )
    }

    /**
     * Deletes a diary by its ID.
     */
    fun deleteDiary(diaryId: String) {
        viewModelScope.launch {
            val result = diaryRepository.deleteDiary(diaryId)
            // If deletion fails, update the error state.
            if (result.isFailure) {
                errorState.value = result.exceptionOrNull()?.message
            }
        }
    }

    /**
     * Resets the edit state to prepare for creating a new diary.
     */
    fun prepareNewDiary() {
        editState.value = DiaryEditUiState()
    }
    
    // --- Search Logic Start ---
    // Holds the current search keyword from the UI.
    var searchQuery = mutableStateOf("")
        private set

    // Updates the search query.
    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    // The master list of all diaries for the current user.
    private var allDiaries = mutableStateOf<List<Diary>>(emptyList())
    
    // The filtered list of diaries to be displayed in the UI, based on the search query.
    var diaryListState = mutableStateOf<List<Diary>>(emptyList())
        private set

    // Filters the diary list based on the search query.
    private fun updateFilteredList() {
        val query = searchQuery.value.lowercase().trim()
        diaryListState.value = if (query.isEmpty()) {
            // If query is empty, show all diaries.
            allDiaries.value
        } else {
            // Otherwise, filter by title or content (case-insensitive).
            allDiaries.value.filter {
                it.title.lowercase().contains(query) || it.content.lowercase().contains(query)
            }
        }
    }
    // --- Search Logic End ---

    // Holds any error message to be displayed in the UI.
    var errorState = mutableStateOf<String?>(null)
        private set

    // Flag to prevent multiple subscriptions to the diary observer.
    private var alreadyObserving = false

    /**
     * Starts observing diary changes from the repository.
     * It's automatically called on login, but is public and guarded
     * by a flag to prevent multiple listeners if called manually.
     */
    fun startObserveDiaries() {
        if (alreadyObserving) return
        alreadyObserving = true

        diaryRepository.observeDiaries(
            onUpdate = { list -> 
                allDiaries.value = list
                updateFilteredList() // Update the UI list whenever data changes.
            },
            onError = { e -> errorState.value = e.message }
        )
    }

    /**
     * Stops observing diary changes and resets the observer flag.
     * Automatically called on logout.
     */
    private fun stopObserveDiaries() {
        diaryRepository.stopObserving()
        alreadyObserving = false
    }

    /**
     * Manually triggers the filtering of diaries based on the current search query.
     * Useful for when the search query is updated.
     */
    fun performSearch() {
        updateFilteredList()
    }
}
