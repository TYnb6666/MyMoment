package nuist.cn.mymoment.model

// Define the data structure of diary entries
data class Diary(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: Long= System.currentTimeMillis()
)
