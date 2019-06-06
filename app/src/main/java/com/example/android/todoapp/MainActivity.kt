package com.example.android.todoapp

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.android.todoapp.network.Task
import com.example.android.todoapp.network.TodoApiFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mTaskList = LinkedList<Task>()

    // Create coroutine Job and Scope
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main )
    private val todoApiService = TodoApiFactory.retrofitService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerView.adapter = TaskListAdapter(mTaskList, this::onDeleteItem, this::onCloseItem, this::onReopenItem)

        fab.setOnClickListener { view ->
            showAddItemDialog()
        }
        getTasks()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getTasks() {
        coroutineScope.launch {
            val getTasksDeferred = todoApiService.getTasks().await()
            Log.i("task", "\uD83D\uDD25\uD83D\uDD25 RESULT => $getTasksDeferred")
            mTaskList.clear()
            mTaskList.addAll(getTasksDeferred)
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun createTasks(str: String) {
        coroutineScope.launch {
            val getTasksDeferred = todoApiService.createTasks(Task("",str, false)).await()
            Log.i("🚨 task", "\uD83D\uDD25\uD83D\uDD25 RESULT => $getTasksDeferred")
            getTasks()
        }
    }

    private fun onDeleteItem(position: Int) {
        lifecycleScope.launch {
            val task = mTaskList[position]
            val res = todoApiService.deleteTask(task.id).await()
            if (res.isSuccessful) {
                mTaskList.remove(task)
                recyclerView.adapter?.notifyItemRemoved(position)
            }
        }
    }

    private fun onCloseItem(position: Int) {
        lifecycleScope.launch {
            val task = mTaskList[position]
            val res = todoApiService.checkedTasks(task.id).await()
            Log.i("🚨 close", "\uD83D\uDD25\uD83D\uDD25 RESULT => $res")
        }

    }

    private fun onReopenItem(position: Int){
        lifecycleScope.launch {
            val task = mTaskList[position]
            val res = todoApiService.unCheckedTasks(task.id).await()
            Log.i("🚨 reopen", "\uD83D\uDD25\uD83D\uDD25 RESULT => $res")
        }
    }

    private fun showAddItemDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add a new task")
            .setMessage("What do you want to do next?")
            .setView(editText)
            .setPositiveButton("Add") { _, _ -> createTasks(editText.text.toString()) }
            .setNegativeButton("Cancel") { _, _-> null }
            .create()
        dialog.show()
    }
}
