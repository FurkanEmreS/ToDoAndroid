package com.xsoftware.todoapppractice.Database

import java.io.Serializable
import java.util.Date

data class TaskItem(
    var name: String = "",
    var desc: String = "",
    var date: Date? = null,
    var time: String? = null,
    var isCompleted: Boolean = false,
    var id: String = "" // Firestore document ID
) : Serializable