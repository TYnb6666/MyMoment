package nuist.cn.mymoment.data.diary

import java.util.UUID
import nuist.cn.mymoment.data.diary.Diary

/**
 * In-memory diary repository for pure front-end demos.
 * Data lives only during app process lifetime and is not synced to any backend.
 */
object DiaryRepository {
    private val diaries = mutableListOf<Diary>()
    private val observers = mutableSetOf<(List<Diary>) -> Unit>()

    init {
        if (diaries.isEmpty()) {
            seedSamples()
        }
    }

    fun subscribe(observer: (List<Diary>) -> Unit): () -> Unit {
        observers += observer
        observer(snapshot())
        return { observers -= observer }
    }

    fun getAll(): List<Diary> = snapshot()

    fun addDiary(diary: Diary): Diary {
        val entry = diary.copy(
            id = diary.id.ifBlank { UUID.randomUUID().toString() },
            date = if (diary.date <= 0) System.currentTimeMillis() else diary.date
        )
        diaries.add(0, entry)
        notifyObservers()
        return entry
    }

    fun updateDiary(diary: Diary): Boolean {
        if (diary.id.isBlank()) return false
        val index = diaries.indexOfFirst { it.id == diary.id }
        if (index == -1) return false
        diaries[index] = diary
        notifyObservers()
        return true
    }

    fun deleteDiary(diaryId: String): Boolean {
        val removed = diaries.removeAll { it.id == diaryId }
        if (removed) notifyObservers()
        return removed
    }

    fun search(keyword: String): List<Diary> {
        val lower = keyword.trim().lowercase()
        if (lower.isEmpty()) return snapshot()
        return diaries.filter {
            it.title.lowercase().contains(lower) ||
                it.content.lowercase().contains(lower) ||
                it.locationName.lowercase().contains(lower)
        }
    }

    private fun notifyObservers() {
        val data = snapshot()
        observers.forEach { it(data) }
    }

    private fun snapshot(): List<Diary> = diaries.sortedByDescending { it.date }

    private fun seedSamples() {
        val sample = listOf(
            Diary(
                id = UUID.randomUUID().toString(),
                title = "Sunset Walk",
                content = "Captured a beautiful sunset near the river and wrote down my thoughts.",
                weather = "Sunny",
                temperature = "24",
                locationName = "Qinhuai Riverside",
                latitude = 32.032,
                longitude = 118.821
            ),
            Diary(
                id = UUID.randomUUID().toString(),
                title = "Rainy Cafe",
                content = "Spent the afternoon reading with a cup of latte while it rained outside.",
                weather = "Rainy",
                temperature = "18",
                locationName = "Local Coffee Shop"
            )
        )
        diaries.addAll(sample)
    }
}
