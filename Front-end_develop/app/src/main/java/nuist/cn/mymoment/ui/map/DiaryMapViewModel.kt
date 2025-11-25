package nuist.cn.mymoment.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nuist.cn.mymoment.data.diary.Diary
import nuist.cn.mymoment.data.diary.DiaryRepository

class DiaryMapViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _markers = MutableLiveData<List<Diary>>(emptyList())
    val markers: LiveData<List<Diary>> = _markers

    private var unsubscribe: (() -> Unit)? = null

    init {
        startObserving()
    }

    private fun startObserving() {
        unsubscribe?.invoke()
        unsubscribe = repository.subscribe { diaries ->
            val points = diaries.filter { it.latitude != null && it.longitude != null }
            _markers.postValue(points)
        }
    }

    override fun onCleared() {
        unsubscribe?.invoke()
        super.onCleared()
    }
}

class DiaryMapViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryMapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryMapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

