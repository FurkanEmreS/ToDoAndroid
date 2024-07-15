package com.xsoftware.todoapppractice

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface TaskDao {
    @Query("SELECT * FROM TaskItem")
    fun getAll(): Flowable<List<TaskItem>>

    @Insert
    fun insert(taskItem: TaskItem): Completable

    @Update
    fun update(taskItem: TaskItem): Completable

    @Delete
    fun delete(taskItem: TaskItem): Completable
}