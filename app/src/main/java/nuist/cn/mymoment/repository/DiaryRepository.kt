package nuist.cn.mymoment.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import nuist.cn.mymoment.model.Diary

class DiaryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore
) {
    // Real-time listener reference
    private var listener: ListenerRegistration? = null

    init {
        // DEV: Use Firebase emulator
        db.useEmulator("10.0.2.2", 8080)
        FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
    }

    // Get current user's diaries collection reference
    private fun userDiariesCollection() =
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("diaries")
        }

    suspend fun addDiary(diary: Diary): Result<Unit> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("User not logged in"))

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

    suspend fun updateDiary(diaryID: String, diary: Diary): Result<Unit> {
        val col = userDiariesCollection()
            ?: return Result.failure(IllegalStateException("User not logged in"))
        return try {
            // Update fields and set new timestamp
            val updates = hashMapOf<String, Any?>(
                "title" to diary.title,
                "content" to diary.content,
                "location" to diary.location,
                "timestamp" to System.currentTimeMillis()
            )
            col.document(diaryID).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDiary(diaryID: String): Result<Unit> {
        val col = userDiariesCollection()
            ?: return Result.failure(IllegalStateException("User not logged in"))
        return try {
            col.document(diaryID).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Start real-time listener for diary updates
     * Returns the ListenerRegistration for cleanup
     */
    fun observeDiaries(
        onUpdate: (List<Diary>) -> Unit,
        onError: (Exception) -> Unit = {}
    ): ListenerRegistration? {
        val col = userDiariesCollection() ?: run { onUpdate(emptyList()); return null }

        listener = col
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { doc ->
                    val d = doc.toObject(Diary::class.java) ?: Diary()
                    d.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(list)
            }
        return listener
    }

    // Stop listening for real-time updates
    fun stopObserving() {
        listener?.remove()
        listener = null
    }
}