package nuist.cn.mymoment.data.diary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Define the data structure of diary entries
@Parcelize
data class Diary(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Long = System.currentTimeMillis(),
    val weather: String = "",
    val temperature: String = "",
    val locationName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) : Parcelable
