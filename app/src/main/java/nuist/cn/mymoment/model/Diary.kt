package nuist.cn.mymoment.model

import com.google.firebase.firestore.GeoPoint

// Define the data structure of diary entries
data class Diary(
    val id: String = "",  // firestore document ID
    val title: String = "",
    val content: String = "",
    val timestamp: Long= System.currentTimeMillis(),
    val location: GeoPoint? = null
)
