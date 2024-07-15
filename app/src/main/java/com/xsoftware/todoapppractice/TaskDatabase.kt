package com.xsoftware.todoapppractice

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [TaskItem::class], version = 3)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}