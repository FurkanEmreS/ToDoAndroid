package com.xsoftware.todoapppractice.UI

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xsoftware.todoapppractice.databinding.FragmentNewTaskSheetBinding
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import com.xsoftware.todoapppractice.Database.TaskItem
import com.xsoftware.todoapppractice.Database.TaskRepository
import com.xsoftware.todoapppractice.Utils.DateUtils
import com.xsoftware.todoapppractice.Notification.NotificationReceiver
import com.xsoftware.todoapppractice.R
import java.text.SimpleDateFormat
import java.util.Locale


class NewTaskFragment : Fragment() {
    private var _binding: FragmentNewTaskSheetBinding? = null
    private val binding get() = _binding!!
    private var taskItem: TaskItem? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            taskItem = it.getSerializable("taskItem") as? TaskItem
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backIcon: ImageButton = view.findViewById(R.id.back_icon)
        val saveIcon: ImageButton = view.findViewById(R.id.save_icon)
        val toolbarText: TextView = view.findViewById(R.id.toolbarText)
        val exitIconButton: ImageButton = view.findViewById(R.id.exit_icon)
        val deleteIconButton: ImageButton = view.findViewById(R.id.delete_icon)
        val timePickerButton = binding.timePickerButton
        val datePickerButton = binding.datePickerButton
        exitIconButton.visibility = View.GONE
        datePickerButton.text = getString(R.string.select_date)
        timePickerButton.text = getString(R.string.select_time)
        val name = binding.name
        val desc = binding.desc
        name.hint = getString(R.string.task_title)
        desc.hint = getString(R.string.task_description)




        binding.saveButton.visibility = View.GONE

        deleteIconButton.setOnClickListener {
            taskItem?.let {
                showCustomDialogBox()
            }
        }

        backIcon.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        toolbarText.setOnClickListener {
            Log.d("NewTaskSheet", "Toolbar clicked")
        }

        saveIcon.setOnClickListener {
            Log.d("NewTaskSheet", "Save clicked")
            val name = binding.name.text.toString().trim()
            val desc = binding.desc.text.toString().trim()
            val date = binding.datePickerButton.text.toString().trim()
            val time = binding.timePickerButton.text.toString().trim()

            if (name.isEmpty()) {
                binding.name.error = getString(R.string.name_required)
                binding.name.requestFocus()
                return@setOnClickListener
            }

            if (desc.isEmpty()) {
                binding.desc.error = getString(R.string.desc_required)
                binding.desc.requestFocus()
                return@setOnClickListener
            }

            if (date == getString(R.string.select_date)) {
                binding.datePickerButton.error = getString(R.string.date_required)
                binding.datePickerButton.requestFocus()
                return@setOnClickListener
            }

            if (time == getString(R.string.select_time)) {
                binding.timePickerButton.error = getString(R.string.time_required)
                binding.timePickerButton.requestFocus()
                return@setOnClickListener
            }

            val parsedDate = DateUtils.parseDate(requireContext(), date)

            val newItem = taskItem?.copy(
                name = name,
                desc = desc,
                date = parsedDate,
                time = time
            ) ?: TaskItem(
                name = name,
                desc = desc,
                date = parsedDate,
                time = time
            )

            if (taskItem == null) {
                TaskRepository.addTask(newItem, {
                    (parentFragment as? HomeFragment)?.taskList?.add(newItem)
                    (parentFragment as? HomeFragment)?.taskAdapter?.notifyItemInserted((parentFragment as? HomeFragment)?.taskList?.size ?: 0 - 1)
                    scheduleNotification(newItem)
                    requireActivity().supportFragmentManager.popBackStack()
                }, { error ->
                    Log.e("NewTaskSheet", "Error: ${error.message}")
                })
            } else {
                TaskRepository.updateTask(newItem, {
                    val index = (parentFragment as? HomeFragment)?.taskList?.indexOfFirst { it.id == newItem.id }
                    if (index != null && index != -1) {
                        (parentFragment as? HomeFragment)?.taskList?.set(index, newItem)
                        (parentFragment as? HomeFragment)?.taskAdapter?.notifyItemChanged(index)
                    }
                    scheduleNotification(newItem)
                    requireActivity().supportFragmentManager.popBackStack()
                }, { error ->
                    Log.e("NewTaskSheet", "Error: ${error.message}")
                })
            }
        }

        if (taskItem != null) {
            binding.name.setText(taskItem!!.name)
            binding.desc.setText(taskItem!!.desc)
            binding.datePickerButton.text = taskItem!!.date?.let {
                DateUtils.formatDate(
                    requireContext(),
                    it
                )
            } ?: getString(R.string.select_date)
            binding.timePickerButton.text = taskItem!!.time ?: getString(R.string.select_time)
            binding.taskTitle.text = getString(R.string.edit_task)
            toolbarText.text = getString(R.string.edit_task)
            binding.deleteButton.visibility = View.GONE
        } else {
            toolbarText.text = getString(R.string.new_task)
            binding.taskTitle.text = getString(R.string.new_task)
            binding.deleteButton.visibility = View.GONE
            deleteIconButton.visibility = View.GONE
        }

        binding.datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    binding.datePickerButton.text = DateUtils.formatDate(
                        requireContext(),
                        DateUtils.parseDate(requireContext(), date)!!
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        binding.timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val time = String.format("%02d:%02d", hourOfDay, minute)
                    binding.timePickerButton.text = time
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        binding.deleteButton.setOnClickListener {
            taskItem?.let {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.title_delete))
                    setMessage(getString(R.string.delete_alert))
                    setPositiveButton(getString(R.string.yes)) { _, _ ->
                        TaskRepository.deleteTask(it.id, {
                            (parentFragment as? HomeFragment)?.taskList?.remove(it)
                            (parentFragment as? HomeFragment)?.taskAdapter?.notifyDataSetChanged()
                            requireActivity().supportFragmentManager.popBackStack()
                        }, { error ->
                            Log.e("NewTaskSheet", "Error: ${error.message}")
                        })
                    }
                    setNegativeButton(getString(R.string.no), null)
                }.show()
            }
        }
    }

    private fun scheduleNotification(taskItem: TaskItem) {
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("title", taskItem.name)
            putExtra("message", taskItem.desc)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            taskItem.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val calendar = Calendar.getInstance().apply {
                time = taskItem.date
                val timeCalendar = Calendar.getInstance().apply {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    time = timeFormat.parse(taskItem.time)
                }
                set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                set(Calendar.SECOND, 0)
            }

            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        tvMessage.text = getString(R.string.delete_alert)
        btnYes.setOnClickListener {
            taskItem?.let {
                TaskRepository.deleteTask(it.id, {
                    (parentFragment as? HomeFragment)?.taskList?.remove(it)
                    (parentFragment as? HomeFragment)?.taskAdapter?.notifyDataSetChanged()
                    requireActivity().supportFragmentManager.popBackStack()
                }, { error ->
                    Log.e("NewTaskSheet", "Error: ${error.message}")
                })
            }
            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}