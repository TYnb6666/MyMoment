package nuist.cn.mymoment.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nuist.cn.mymoment.repository.DiaryRepository

data class DiaryEditUiState(
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class DiaryViewModel (private val repository: DiaryRepository = DiaryRepository()) : ViewModel(){
    var editState = mutableStateOf(DiaryEditUiState())
        private set

    fun onTitleChange(newTitle: String){
        editState.value = editState.value.copy(title = newTitle)
    }

    fun onContentChange(newContent: String){
        editState.value = editState.value.copy(content = newContent)
    }

    fun saveDiary(onSuccess: () -> Unit = {}) {
        val state = editState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            editState.value = state.copy(error = "Title or content cannot be empty")
            return
        }

        viewModelScope.launch {
            editState.value = state.copy(isSaving = true, error = null, saveSuccess = false)
            val result = repository.addDiary(state.title, state.content) // timestamp will be automatically added in the addDiary method
            editState.value = if (result.isSuccess) {
                state.copy(
                    isSaving = false,
                    error = null,
                    saveSuccess = true
                )
            } else {
                state.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message,
                    saveSuccess = false
                )
            }

            if (result.isSuccess) {
                onSuccess()
            }
        }
    }

    fun resetAfterSaved() {
        //  clear input and reset status
        editState.value = DiaryEditUiState()
    }


}