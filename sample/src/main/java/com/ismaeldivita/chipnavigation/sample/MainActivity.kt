package com.ismaeldivita.chipnavigation.sample

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

//import android.app.ListActivity
//import android.content.Intent
//import android.os.Bundle
//import android.view.View
//import android.widget.ListView
//import com.ismaeldivita.chipnavigation.sample.util.applyWindowInsets
//import android.widget.ArrayAdapter
//
//class MainActivity : ListActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val list = ListView(this).apply {
//            id = android.R.id.list
//            applyWindowInsets(top = true)
//        }
//        setContentView(list)
//
//        val entities = arrayOf("수평 네비게이션바", "수직 네비게이션바")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, entities)
//        listAdapter = adapter
//        startActivity(Intent(this, VerticalModeActivity::class.java))
//    }
//
//    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
//        when (position) {
//            0 -> startActivity(Intent(this, HorizontalModeActivity::class.java))
//            1  -> startActivity(Intent(this, VerticalModeActivity::class.java))
//        }
//    }
//
//}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_vertical)
        startActivity(Intent(this, VerticalModeActivity::class.java))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}