package nuist.cn.mymoment.ui.diary.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nuist.cn.mymoment.data.diary.Diary
import nuist.cn.mymoment.data.diary.DiaryRepository

class DiaryListViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _diaries = MutableLiveData<List<Diary>>(emptyList())
    val diaries: LiveData<List<Diary>> = _diaries

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var unsubscribe: (() -> Unit)? = null

    init {
        startObserving()
    }

    private fun startObserving() {
        _isLoading.value = true
        unsubscribe?.invoke()
        unsubscribe = repository.subscribe {
            _isLoading.postValue(false)
            _diaries.postValue(it)
        }
    }

    fun refresh() {
        _diaries.postValue(repository.getAll())
    }

    fun search(keyword: String) {
        _diaries.postValue(repository.search(keyword))
    }

    fun deleteDiary(diaryId: String, onResult: (Boolean) -> Unit) {
        onResult(repository.deleteDiary(diaryId))
    }

    override fun onCleared() {
        unsubscribe?.invoke()
        super.onCleared()
    }
}

class DiaryListViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

