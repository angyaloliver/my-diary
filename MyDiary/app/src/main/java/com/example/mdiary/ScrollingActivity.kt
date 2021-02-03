package com.example.mdiary

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.mdiary.adapter.DiaryAdapter
import com.example.mdiary.data.AppDatabase
import com.example.mdiary.data.DiaryItem
import kotlinx.android.synthetic.main.activity_scrolling.*


class ScrollingActivity : AppCompatActivity(), DiaryDialog.DiaryHandler {

    lateinit var diaryAdapter: DiaryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(toolbar)
        fab.setOnClickListener { view ->
            DiaryDialog().show(supportFragmentManager,
                "DIARY_DIALOG")
        }

        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_delete_all -> {
                deleteAllDiaryItems()
                return true
            }
            R.id.action_show_map -> {
                navigateToMapsActivity()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun initRecyclerView() {
        Thread {
            var diaryItemList =
                AppDatabase.getInstance(this@ScrollingActivity).diaryItemDAO().getAllDiaryItems()

            runOnUiThread {
                diaryAdapter = DiaryAdapter(this, diaryItemList)

                var itemDecorator = DividerItemDecoration(this,
                    DividerItemDecoration.VERTICAL)
                recyclerDiary.addItemDecoration(itemDecorator)

                recyclerDiary.adapter = diaryAdapter
            }

        }.start()
    }

    override fun diaryItemCreated(diaryItem: DiaryItem) {
        Thread {
            var newId = AppDatabase.getInstance(this@ScrollingActivity).diaryItemDAO().addDiaryItem(
                diaryItem
            )

            diaryItem.diaryItemId = newId

            runOnUiThread{
                diaryAdapter.addDiaryItem(diaryItem)
            }

        }.start()
    }

    fun deleteAllDiaryItems(){
        diaryAdapter.deleteAll()
    }

    fun navigateToMapsActivity(){
        startActivity(Intent(this,MapsActivity::class.java))
    }
}
