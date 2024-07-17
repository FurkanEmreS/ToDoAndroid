package com.xsoftware.todoapppractice

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.xsoftware.todoapppractice.databinding.ItemCellBinding

class TaskAdapter(
    private val taskList: List<TaskItem>,
    private val onDeleteClick: (TaskItem) -> Unit,
    private val onItemClick: (TaskItem) -> Unit,
    private val onCompleteClick: (TaskItem) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    class TaskHolder(val itemCellBinding: ItemCellBinding) : RecyclerView.ViewHolder(itemCellBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val itemCellBinding = ItemCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskHolder(itemCellBinding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val taskItem = taskList[position]
        holder.itemCellBinding.name.text = taskItem.name
        holder.itemCellBinding.dueTime.text = taskItem.time

        if (taskItem.isCompleted) {
            holder.itemCellBinding.name.paintFlags = holder.itemCellBinding.name.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemCellBinding.completeButton.setImageResource(R.drawable.checked)
        } else {
            holder.itemCellBinding.name.paintFlags = holder.itemCellBinding.name.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemCellBinding.completeButton.setImageResource(R.drawable.notcheck)
        }


        holder.itemCellBinding.completeButton.setOnClickListener {
            taskItem.isCompleted = !taskItem.isCompleted
            onCompleteClick(taskItem)
            notifyItemChanged(position)
        }

        holder.itemCellBinding.deleteButton.setOnClickListener {
            showCustomDialogBox(holder.itemView.context,taskItem)


        }
        holder.itemView.setOnClickListener {
            onItemClick(taskItem)
        }



    }

    private fun showCustomDialogBox(context: Context?, taskItem: TaskItem) {
        val dialog =Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage : TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes : TextView = dialog.findViewById(R.id.btnYes)
        val btnNo : TextView = dialog.findViewById(R.id.btnNo)
        tvMessage.text = "Are you sure you want to delete this task?"
        btnYes.setOnClickListener {
            onDeleteClick(taskItem)
            dialog.dismiss()

        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
dialog.show()


    }

    private fun showDeleteConfirmationDialog(context: Context, taskItem: TaskItem) {
        AlertDialog.Builder(context).apply {
            setTitle("Delete Task")
            setMessage("Are you sure you want to delete this task?")
            setPositiveButton("Yes") { _, _ ->
                onDeleteClick(taskItem)
            }
            setNegativeButton("No", null)
        }.show()
    }


}