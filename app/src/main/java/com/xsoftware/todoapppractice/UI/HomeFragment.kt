package com.xsoftware.todoapppractice.UI

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.xsoftware.todoapppractice.Database.TaskItem
import com.xsoftware.todoapppractice.Database.TaskRepository
import com.xsoftware.todoapppractice.R
import com.xsoftware.todoapppractice.UI.Adapter.TaskAdapter
import com.xsoftware.todoapppractice.databinding.FragmentNewTaskBinding


class HomeFragment : Fragment() {
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
        var exiticon  :ImageButton= view.findViewById(R.id.exit_icon)
        val newTaskButton = binding.newTaskButton
        val quitButton = binding.exitButton
        quitButton.visibility = View.GONE
        newTaskButton.text = getString(R.string.new_task)
        quitButton.text = getString(R.string.quit)


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
                val fragment = NewTaskFragment()
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

        binding.exitButton.setOnClickListener {
            showCustomDialogBox()

        }
        exiticon.setOnClickListener {
            showCustomDialogBox()
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
        val fragment = NewTaskFragment()
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


    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: TextView = dialog.findViewById(R.id.btnYes)
        val btnNo: TextView = dialog.findViewById(R.id.btnNo)
        btnYes.text = getString(R.string.yes)
        btnNo.text = getString(R.string.no)
        tvMessage.text = getString(R.string.quit_alert)
        btnYes.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}