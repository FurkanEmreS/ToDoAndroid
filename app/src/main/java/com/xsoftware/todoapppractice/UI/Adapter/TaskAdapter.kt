package com.xsoftware.todoapppractice.UI.Adapter

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
import com.xsoftware.todoapppractice.Database.TaskItem
import com.xsoftware.todoapppractice.R
import com.xsoftware.todoapppractice.databinding.ItemCellBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        val currentDate = Calendar.getInstance().time
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val taskDate = taskItem.date
        val taskTimeStr = taskItem.time

        if (taskDate != null) {
            val taskDateOnly = dateOnlyFormat.format(taskDate)
            val currentDateOnly = dateOnlyFormat.format(currentDate)
            val currentTimeOnly = timeFormat.format(currentDate)

            when {
                taskDateOnly == currentDateOnly -> {
                    if (taskTimeStr != null && taskTimeStr <= currentTimeOnly) {
                        holder.itemCellBinding.name.setTextColor(Color.RED)
                    } else {
                        holder.itemCellBinding.name.setTextColor(Color.GREEN)
                    }
                }
                taskDate.before(currentDate) -> {
                    holder.itemCellBinding.name.setTextColor(Color.RED)
                }
                else -> {
                    holder.itemCellBinding.name.setTextColor(Color.BLACK)
                }
            }
        } else {
            holder.itemCellBinding.name.setTextColor(Color.BLACK)
        }

        holder.itemCellBinding.completeButton.setOnClickListener {
            taskItem.isCompleted = !taskItem.isCompleted
            onCompleteClick(taskItem)
            notifyItemChanged(position)
        }

        holder.itemCellBinding.deleteButton.setOnClickListener {
            showCustomDialogBox(holder.itemView.context, taskItem) // Burada holder.itemView.context kullanıyoruz
        }

        holder.itemView.setOnClickListener {
            onItemClick(taskItem)
        }
    }

    private fun showCustomDialogBox(context: Context, taskItem: TaskItem) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: TextView = dialog.findViewById(R.id.btnYes)
        val btnNo: TextView = dialog.findViewById(R.id.btnNo)
        btnYes.text = context.getString(R.string.yes)
        btnNo.text = context.getString(R.string.no)
        tvMessage.text = context.getString(R.string.delete_alert) // Burada context.getString kullanıyoruz
        btnYes.setOnClickListener {
            onDeleteClick(taskItem)
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}