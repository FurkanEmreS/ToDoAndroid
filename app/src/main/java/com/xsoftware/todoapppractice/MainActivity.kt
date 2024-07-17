package com.xsoftware.todoapppractice

import android.app.FragmentTransaction
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.xsoftware.todoapppractice.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import android.util.Log
import android.widget.ImageView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable = CompositeDisposable()
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<TaskItem>()
    val newTaskSubject = PublishSubject.create<TaskItem>()
    val deleteTaskSubject = PublishSubject.create<TaskItem>()
    val updateTaskSubject = PublishSubject.create<TaskItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()
        var leftIcon :ImageView = findViewById(R.id.left_icon)
        leftIcon.visibility = View.GONE
        var rightIcon :ImageView = findViewById(R.id.right_icon)
        rightIcon.visibility = View.GONE





        val db = Room.databaseBuilder(applicationContext, TaskDatabase::class.java, "TaskDatabase")
            .fallbackToDestructiveMigration()
            .build()
        val taskDao = db.taskDao()

        taskAdapter = TaskAdapter(taskList, { taskItem ->
            deleteTaskSubject.onNext(taskItem)
        }, { taskItem ->
            showEditTaskSheet(taskItem)
        }, { taskItem ->
            updateTaskSubject.onNext(taskItem)
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = taskAdapter

        compositeDisposable.add(
            taskDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, { error ->
                    Log.e("MainActivity", "Error: ${error.message}")
                })
        )

        binding.newTaskButton.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("newTaskTag") == null) {
                val fragment = NewTaskSheet()
                supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right

                )
                    .replace(android.R.id.content, fragment, "newTaskTag")
                    .addToBackStack(null)
                    .commit()
            } else {
                supportFragmentManager.popBackStack()
            }
        }

        compositeDisposable.add(
            newTaskSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { newTask ->
                    taskList.add(newTask)
                    taskAdapter.notifyItemInserted(taskList.size - 1)
                }
        )

        compositeDisposable.add(
            deleteTaskSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { taskItem ->
                    compositeDisposable.add(
                        taskDao.delete(taskItem)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                taskList.remove(taskItem)
                                taskAdapter.notifyDataSetChanged()
                            }, { error ->
                                Log.e("MainActivity", "Error: ${error.message}")
                            })
                    )
                }
        )


        compositeDisposable.add(
            updateTaskSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updatedTask ->
                    val index = taskList.indexOfFirst { it.id == updatedTask.id }
                    if (index != -1) {
                        compositeDisposable.add(
                            taskDao.update(updatedTask)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    taskList[index] = updatedTask
                                    taskAdapter.notifyItemChanged(index)
                                }, { error ->
                                    Log.e("MainActivity", "Error: ${error.message}")
                                })
                        )
                    }
                }
        )
    }

    private fun handleResponse(taskList: List<TaskItem>) {
        this.taskList.clear()
        this.taskList.addAll(taskList)
        taskAdapter.notifyDataSetChanged()
    }

    private fun showEditTaskSheet(taskItem: TaskItem) {
        val fragment = NewTaskSheet()
        val args = Bundle()
        args.putSerializable("taskItem", taskItem)
        fragment.arguments = args
        supportFragmentManager.beginTransaction().setCustomAnimations(
           R.anim.left_slide_in_left,
            R.anim.left_slide_out_right,
            R.anim.left_slide_in_right,
            R.anim.left_slide_out_left
        )
            .replace(android.R.id.content, fragment, "editTaskTag")
            .addToBackStack(null)
            .commit()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Channel"
            val descriptionText = "Channel for Task Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}