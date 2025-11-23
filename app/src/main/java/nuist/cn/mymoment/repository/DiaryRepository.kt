package nuist.cn.mymoment.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import nuist.cn.mymoment.model.Diary

class DiaryRepository {
    private val db = Firebase.firestore.collection("diaries")

    fun addDiary(diary: Diary){
        db.add(diary)
    }

    fun getAllDiaries(callback: (List<Diary>) -> Unit) {
        db.get().addOnSuccessListener { result ->
            val list = result.map { it.toObject(Diary::class.java) }
            callback(list)
        }
    }
}