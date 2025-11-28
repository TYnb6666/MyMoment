package nuist.cn.mymoment.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import nuist.cn.mymoment.model.Diary

class DiaryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore
) {

    init {
        // connect to local Emulator
        db.useEmulator("10.0.2.2", 8080)
    }

    private fun userDiariesCollection() =
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("diaries")
        }

    suspend fun addDiary(title: String, content: String): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("User not logged in"))

        val diary = hashMapOf(
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        return try {
            userDiariesCollection()
                ?.add(diary)
                ?.await()
                ?: return Result.failure(IllegalStateException("User collection not found"))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}