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
    var taskList = mutableListOf<TaskItem>()
    private var _binding: FragmentNewTaskBinding? = null
    private val binding get() = _binding!!
    lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var leftIcon: ImageView = view.findViewById(R.id.left_icon)
        leftIcon.visibility = View.GONE
        var rightIcon: ImageView = view.findViewById(R.id.right_icon)
        rightIcon.visibility = View.GONE
        var deleteImageIcon: ImageButton = view.findViewById(R.id.delete_icon)
        deleteImageIcon.visibility = View.GONE

        taskAdapter = TaskAdapter(taskList, { taskItem ->
            deleteTask(taskItem)
        }, { taskItem ->
            showEditTaskSheet(taskItem)
        }, { taskItem ->
            updateTask(taskItem)
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = taskAdapter

        TaskRepository.getTasks({ tasks ->
            handleResponse(tasks)
        }, { error ->
            Log.e("NewTaskFragment", "Error: ${error.message}")
        })

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
    }

    private fun deleteTask(taskItem: TaskItem) {
        TaskRepository.deleteTask(taskItem.id, {
            taskList.remove(taskItem)
            taskAdapter.notifyDataSetChanged()
        }, { error ->
            Log.e("NewTaskFragment", "Error: ${error.message}")
        })
    }

    private fun updateTask(taskItem: TaskItem) {
        TaskRepository.updateTask(taskItem, {
            val index = taskList.indexOfFirst { it.id == taskItem.id }
            if (index != -1) {
                taskList[index] = taskItem
                taskAdapter.notifyItemChanged(index)
            }
        }, { error ->
            Log.e("NewTaskFragment", "Error: ${error.message}")
        })
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}