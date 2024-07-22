package com.xsoftware.todoapppractice

import java.io.Serializable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

data class TaskItem(
    var name: String = "",
    var desc: String = "",
    var date: Date? = null,
    var time: String? = null,
    var isCompleted: Boolean = false,
    var id: String = "" // Firestore document ID
) : Serializable