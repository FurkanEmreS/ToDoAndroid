package com.xsoftware.todoapppractice

import java.io.Serializable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class TaskItem(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "desc")
    var desc: String,
    @ColumnInfo(name = "date")
    var date: Date? = null,
    @ColumnInfo(name = "time")
    var time: String? = null,
    @ColumnInfo
    var isCompleted: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
) : Serializable