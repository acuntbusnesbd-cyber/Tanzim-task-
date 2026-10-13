package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.DeveloperInfo
import com.example.data.model.ClassRoutineItem
import com.example.data.model.TeacherInfo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreService(context: Context) {
  private val db = FirebaseFirestore.getInstance(
    context.getString(R.string.firestore_database_id)
  )

  // Collections
  private val routineOverrides = db.collection("routine_overrides")
  private val teachers = db.collection("teachers")
  private val notices = db.collection("notices")

  // Observe all routine overrides
  fun observeRoutineOverrides(): Flow<Map<String, PartialRoutineItem>> {
    return routineOverrides.snapshots().map { snapshot ->
      snapshot.documents.mapNotNull { doc ->
        val data = doc.toObject(PartialRoutineItem::class.java)
        if (data != null) doc.id to data else null
      }.toMap()
    }
  }

  // Observe all teachers
  fun observeTeachers(): Flow<List<TeacherInfo>> {
    return teachers.snapshots().map { snapshot ->
      snapshot.toObjects(TeacherInfo::class.java)
    }
  }

  // Observe developer info (notices)
  fun observeDeveloperInfo(): Flow<DeveloperInfo?> {
    return notices.document("current_announcement").snapshots().map { snapshot ->
      snapshot.toObject(DeveloperInfo::class.java)
    }
  }

  // Admin writes
  suspend fun saveRoutineOverride(item: ClassRoutineItem) {
    val data = mapOf(
      "subjectName" to item.subjectName,
      "roomNo" to item.roomNo,
      "teacherName" to item.teacherName,
      "teacherCode" to item.teacherCode,
      "teacherPhone" to item.teacherPhone,
      "isFreePeriod" to item.isFreePeriod
    )
    routineOverrides.document(item.id).set(data).await()
  }

  suspend fun saveTeacher(teacher: TeacherInfo) {
    teachers.document(teacher.id).set(teacher).await()
  }

  suspend fun saveDeveloperInfo(info: DeveloperInfo) {
    notices.document("current_announcement").set(info).await()
  }

  suspend fun resetAll(defaultTeachers: List<TeacherInfo>) {
    // Clear overrides
    val overrides = routineOverrides.get().await()
    for (doc in overrides.documents) {
      doc.reference.delete().await()
    }
    
    // Reset teachers to defaults in cloud too
    val currentTeachers = teachers.get().await()
    for (doc in currentTeachers.documents) {
      doc.reference.delete().await()
    }
    for (teacher in defaultTeachers) {
      saveTeacher(teacher)
    }
  }
}

// Helper data class for partial updates to routine items
data class PartialRoutineItem(
  val subjectName: String = "",
  val roomNo: String = "",
  val teacherName: String = "",
  val teacherCode: String = "",
  val teacherPhone: String = "",
  val isFreePeriod: Boolean = false
)
