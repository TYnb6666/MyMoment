package nuist.cn.mymoment.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = Firebase.firestore
) {

    fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    init {
        // !!! 仅在测试/开发环境使用 !!!
        // 默认端口是 9099，地址 10.0.2.2 是 Android 模拟器访问电脑本地的地址
        try {
            auth.useEmulator("10.0.2.2", 9099)
            db.useEmulator("10.0.2.2", 8080)
        } catch (e: Exception) {
            // 确保只运行一次或在特定条件下运行
            // 如果模拟器未启动，这行代码可能会抛出异常
            e.printStackTrace()
        }
    }

    val currentUser get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                val userDoc = mapOf(
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users")
                    .document(user.uid)
                    .set(userDoc)
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}