package com.xsoftware.todoapppractice

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.xsoftware.todoapppractice.databinding.FragmentNewTaskBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject




class NewTaskFragment : Fragment() {
    private var _binding: FragmentNewTaskBinding? = null
    private val binding get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<TaskItem>()
    val newTaskSubject = PublishSubject.create<TaskItem>()
    val deleteTaskSubject = PublishSubject.create<TaskItem>()
    val updateTaskSubject = PublishSubject.create<TaskItem>()
    private lateinit var db: TaskDatabase
    private lateinit var taskDao: TaskDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createNotificationChannel()
        var leftIcon: ImageView = view.findViewById(R.id.left_icon)
        leftIcon.visibility = View.GONE
        var rightIcon: ImageView = view.findViewById(R.id.right_icon)
        rightIcon.visibility = View.GONE
        var deleteImageIcon :ImageButton = view.findViewById(R.id.delete_icon)
        deleteImageIcon.visibility = View.GONE

        db = Room.databaseBuilder(requireContext().applicationContext, TaskDatabase::class.java, "TaskDatabase")
            .fallbackToDestructiveMigration()
            .build()
        taskDao = db.taskDao()

        taskAdapter = TaskAdapter(taskList, { taskItem ->
            deleteTaskSubject.onNext(taskItem)
        }, { taskItem ->
            showEditTaskSheet(taskItem)
        }, { taskItem ->
            updateTaskSubject.onNext(taskItem)
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = taskAdapter

        compositeDisposable.add(
            taskDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, { error ->
                    Log.e("NewTaskFragment", "Error: ${error.message}")
                })
        )

        binding.newTaskButton.setOnClickListener {
            if (parentFragmentManager.findFragmentByTag("newTaskTag") == null) {
                val fragment = NewTaskSheet()
                parentFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                    .replace(R.id.fragment_container, fragment, "newTaskTag")
                    .addToBackStack(null)
                    .commit()
            } else {
                parentFragmentManager.popBackStack()
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
                                Log.e("NewTaskFragment", "Error: ${error.message}")
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
                                    Log.e("NewTaskFragment", "Error: ${error.message}")
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
        parentFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.left_slide_in_left,
            R.anim.left_slide_out_right,
            R.anim.left_slide_in_right,
            R.anim.left_slide_out_left
        )
            .replace(R.id.fragment_container, fragment, "editTaskTag")
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
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }
}