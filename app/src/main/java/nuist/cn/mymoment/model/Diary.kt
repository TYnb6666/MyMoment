package nuist.cn.mymoment.model

// Define the data structure of diary entries
data class Diary(
    val id: String = "",  // firestore document ID
    val title: String = "",
    val content: String = "",
    val timestamp: Long= System.currentTimeMillis()
)
