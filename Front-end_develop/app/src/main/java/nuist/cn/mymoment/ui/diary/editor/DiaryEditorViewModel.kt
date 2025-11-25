package nuist.cn.mymoment.ui.diary.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nuist.cn.mymoment.data.diary.Diary
import nuist.cn.mymoment.data.diary.DiaryRepository

data class DiaryEditorState(
    val initialDiary: Diary? = null
)

sealed interface DiaryEditorResult {
    data object Created : DiaryEditorResult
    data object Updated : DiaryEditorResult
    data object Failed : DiaryEditorResult
}

class DiaryEditorViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _editorState = MutableLiveData(DiaryEditorState())
    val editorState: LiveData<DiaryEditorState> = _editorState

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    fun loadDiary(diary: Diary?) {
        _editorState.value = DiaryEditorState(initialDiary = diary)
    }

    fun saveDiary(diary: Diary, onResult: (DiaryEditorResult) -> Unit) {
        _isSaving.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = if (diary.id.isBlank()) {
                repository.addDiary(diary)
                DiaryEditorResult.Created
            } else if (repository.updateDiary(diary)) {
                DiaryEditorResult.Updated
            } else {
                DiaryEditorResult.Failed
            }
            _isSaving.postValue(false)
            onResult(result)
        }
    }
}

class DiaryEditorViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryEditorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

