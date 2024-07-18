package com.xsoftware.todoapppractice

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.xsoftware.todoapppractice.databinding.ItemCellBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class TaskAdapter(
    private val taskList: List<TaskItem>,
    private val onDeleteClick: (TaskItem) -> Unit,
    private val onItemClick: (TaskItem) -> Unit,
    private val onCompleteClick: (TaskItem) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskHolder>() {

    private val handler = Handler(Looper.getMainLooper())
    private val timer: Timer = Timer()


    // Verileri bir zamanlayıcı yardımıyla güncelleme
    init {
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post {
                    notifyDataSetChanged() // Verileri güncelle
                }
            }
        }, 0, 10000) // Her 10 saniyede bir kontrol et
    }

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
        //Mevcut tarihi al
        val currentDate = Calendar.getInstance().time
        //Tarih ve saat formatları
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        // Lokal Değişkenler = TaskItem'dan tarih ve saat al
        val taskDateStr = taskItem.date
        val taskTimeStr = taskItem.time

        val taskDate: Date? = try {
            when (taskDateStr) {
                //Eğer taskDateStr "Today" ise onu bugünün tarihi olarak al
                "Today" -> {
                    val today = Calendar.getInstance()
                    dateFormat.parse("${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.YEAR)} $taskTimeStr")
                }
                //Eğer taskDateStr "Tomorrow" ise onu yarının tarihi olarak al
                "Tomorrow" -> {
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                    dateFormat.parse("${tomorrow.get(Calendar.DAY_OF_MONTH)}/${tomorrow.get(Calendar.MONTH) + 1}/${tomorrow.get(Calendar.YEAR)} $taskTimeStr")
                }
                else -> dateFormat.parse("$taskDateStr $taskTimeStr")
            }
            //exception olursa null döndür
        } catch (e: ParseException) {
            null
        }

        if (taskDate != null) {

            //Sadece tarihl olan format
            val taskDateOnly = dateOnlyFormat.format(taskDate)

            val currentDateOnly = dateOnlyFormat.format(currentDate)

            //sadece saat olan format
            val currentTimeOnly = timeFormat.format(currentDate)

            when {
                taskDateOnly == currentDateOnly -> {

                    if (taskTimeStr != null && taskTimeStr <= currentTimeOnly) {
                        holder.itemCellBinding.name.setTextColor(Color.RED)

                    } else {
                        holder.itemCellBinding.name.setTextColor(Color.GREEN) // Bugün ve saat geçmediyse

                    }
                }
                taskDate.before(currentDate) -> {
                    holder.itemCellBinding.name.setTextColor(Color.RED) // Tarih geçtiyse
                }
                else -> {
                    holder.itemCellBinding.name.setTextColor(Color.BLACK) // Sonraki bir tarih
                }
            }
        } else {
            holder.itemCellBinding.name.setTextColor(Color.BLACK) // Varsayılan renk
        }


        holder.itemCellBinding.completeButton.setOnClickListener {
            taskItem.isCompleted = !taskItem.isCompleted
            onCompleteClick(taskItem)
            notifyItemChanged(position)
        }

        holder.itemCellBinding.deleteButton.setOnClickListener {
            showCustomDialogBox(holder.itemView.context, taskItem)
        }

        holder.itemView.setOnClickListener {
            onItemClick(taskItem)
        }
    }

    private fun showCustomDialogBox(context: Context?, taskItem: TaskItem) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvMessage: TextView = dialog.findViewById(R.id.tvMessage)
        val btnYes: TextView = dialog.findViewById(R.id.btnYes)
        val btnNo: TextView = dialog.findViewById(R.id.btnNo)
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