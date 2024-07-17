package com.xsoftware.todoapppractice

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xsoftware.todoapppractice.databinding.FragmentNewTaskSheetBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class NewTaskSheet : Fragment() {
    private var _binding: FragmentNewTaskSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: TaskDatabase
    private lateinit var taskDao: TaskDao
    private val compositeDisposable = CompositeDisposable()
    private var taskItem: TaskItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        db = Room.databaseBuilder(requireContext().applicationContext, TaskDatabase::class.java, "TaskDatabase")
            .fallbackToDestructiveMigration()
            .build()
        taskDao = db.taskDao()

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
        var backIcon :ImageButton = view.findViewById(R.id.left_icon)
        var saveIcon :ImageButton = view.findViewById(R.id.right_icon)
        var toolbarText: TextView = view.findViewById(R.id.toolbarText)

        binding.saveButton.visibility = View.GONE


        backIcon.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        toolbarText.setOnClickListener{
            Log.d("NewTaskSheet", "Toolbar clicked")
        }

        saveIcon.setOnClickListener{

            Log.d("NewTaskSheet", "Save clicked")
            val name = binding.name.text.toString().trim()
            val desc = binding.desc.text.toString().trim()
            val date = binding.datePickerButton.text.toString().trim()
            val time = binding.timePickerButton.text.toString().trim()

            if (name.isEmpty()) {
                binding.name.error = "Name is required"
                binding.name.requestFocus()
                return@setOnClickListener
            }

            if (desc.isEmpty()) {
                binding.desc.error = "Description is required"
                binding.desc.requestFocus()
                return@setOnClickListener
            }

            if (date == "Select Date") {
                binding.datePickerButton.error = "Date is required"
                binding.datePickerButton.requestFocus()
                return@setOnClickListener
            }

            if (time == "Select Time") {
                binding.timePickerButton.error = "Time is required"
                binding.timePickerButton.requestFocus()
                return@setOnClickListener
            }

            val newItem = taskItem?.copy(
                name = name,
                desc = desc,
                date = date,
                time = time
            ) ?: TaskItem(
                name = name,
                desc = desc,
                date = date,
                time = time
            )

            val completable = if (taskItem == null) {
                taskDao.insert(newItem)
            } else {
                taskDao.update(newItem)
            }

            compositeDisposable.add(
                completable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (taskItem == null) {
                            (activity as? MainActivity)?.newTaskSubject?.onNext(newItem)
                        } else {
                            (activity as? MainActivity)?.updateTaskSubject?.onNext(newItem)
                        }
                        scheduleNotification(newItem)
                        requireActivity().supportFragmentManager.popBackStack()
                    }, { error ->
                        Log.e("NewTaskSheet", "Error: ${error.message}")
                    })
            )

        }


        if (taskItem != null) {
            binding.name.setText(taskItem!!.name)
            binding.desc.setText(taskItem!!.desc)
            binding.datePickerButton.text = taskItem!!.date ?: "Select Date"
            binding.timePickerButton.text = taskItem!!.time ?: "Select Time"
            binding.taskTitle.text = "Edit Task"
            toolbarText.text= "Edit Text"


        } else {
            toolbarText.text = "New Task"
            binding.taskTitle.text = "New Task"
            binding.deleteButton.visibility = View.GONE
        }

        binding.datePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = "$dayOfMonth/${month + 1}/$year"
                    binding.datePickerButton.text = date
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

        binding.saveButton.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val desc = binding.desc.text.toString().trim()
            val date = binding.datePickerButton.text.toString().trim()
            val time = binding.timePickerButton.text.toString().trim()

            if (name.isEmpty()) {
                binding.name.error = "Name is required"
                binding.name.requestFocus()
                return@setOnClickListener
            }

            if (desc.isEmpty()) {
                binding.desc.error = "Description is required"
                binding.desc.requestFocus()
                return@setOnClickListener
            }

            if (date == "Select Date") {
                binding.datePickerButton.error = "Date is required"
                binding.datePickerButton.requestFocus()
                return@setOnClickListener
            }

            if (time == "Select Time") {
                binding.timePickerButton.error = "Time is required"
                binding.timePickerButton.requestFocus()
                return@setOnClickListener
            }

            val newItem = taskItem?.copy(
                name = name,
                desc = desc,
                date = date,
                time = time
            ) ?: TaskItem(
                name = name,
                desc = desc,
                date = date,
                time = time
            )

            val completable = if (taskItem == null) {
                taskDao.insert(newItem)
            } else {
                taskDao.update(newItem)
            }

            compositeDisposable.add(
                completable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (taskItem == null) {
                            (activity as? MainActivity)?.newTaskSubject?.onNext(newItem)
                        } else {
                            (activity as? MainActivity)?.updateTaskSubject?.onNext(newItem)
                        }
                        scheduleNotification(newItem)
                        requireActivity().supportFragmentManager.popBackStack()
                    }, { error ->
                        Log.e("NewTaskSheet", "Error: ${error.message}")
                    })
            )
        }

        binding.deleteButton.setOnClickListener {
            taskItem?.let {
                compositeDisposable.add(
                    taskDao.delete(it)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            (activity as? MainActivity)?.deleteTaskSubject?.onNext(taskItem!!)
                            requireActivity().supportFragmentManager.popBackStack()
                        }, { error ->
                            Log.e("NewTaskSheet", "Error: ${error.message}")
                        })
                )
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
            taskItem.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateParts = taskItem.date?.split("/")?.map { it.toInt() } ?: return
        val timeParts = taskItem.time?.split(":")?.map { it.toInt() } ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, dateParts[2])
            set(Calendar.MONTH, dateParts[1] - 1)
            set(Calendar.DAY_OF_MONTH, dateParts[0])
            set(Calendar.HOUR_OF_DAY, timeParts[0])
            set(Calendar.MINUTE, timeParts[1])
            set(Calendar.SECOND, 0)
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }
}
