package com.xsoftware.todoapppractice

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object TaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    fun addTask(task: TaskItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        user?.let {
            db.collection("users").document(it.uid).collection("tasks")
                .add(task)
                .addOnSuccessListener { documentReference ->
                    task.id = documentReference.id // Belge ID'sini alıyoruz
                    onSuccess()
                }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun updateTask(task: TaskItem, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        user?.let {
            db.collection("users").document(it.uid).collection("tasks").document(task.id)
                .set(task, SetOptions.merge())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun deleteTask(taskId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        user?.let {
            db.collection("users").document(it.uid).collection("tasks").document(taskId)
                .delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }

    fun getTasks(onSuccess: (List<TaskItem>) -> Unit, onFailure: (Exception) -> Unit) {
        user?.let {
            db.collection("users").document(it.uid).collection("tasks")
                .get()
                .addOnSuccessListener { result ->
                    val tasks = result.map { document ->
                        val task = document.toObject(TaskItem::class.java)
                        task.id = document.id // Belge ID'sini alıyoruz
                        task
                    }
                    onSuccess(tasks)
                }
                .addOnFailureListener { e -> onFailure(e) }
        }
    }
}