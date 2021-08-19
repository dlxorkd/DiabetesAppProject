package com.ismaeldivita.chipnavigation.sample

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.ismaeldivita.chipnavigation.sample.util.colorAnimation

class HorizontalModeActivity : AppCompatActivity() {

    private val container by lazy { findViewById<View>(R.id.container) }
    private val title by lazy { findViewById<TextView>(R.id.title) }
    private val menu by lazy { findViewById<ChipNavigationBar>(R.id.bottom_menu) }

    private var lastColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horizontal)

        lastColor = (container.background as ColorDrawable).color

        menu.setOnItemSelectedListener { id ->
            val option = when (id) {
                R.id.main -> R.color.main to "메인화면"
                R.id.activity -> R.color.activity to "운동기록"
                R.id.food -> R.color.food to "섭취음식"
                R.id.data -> R.color.data to "데이터"
                else -> R.color.white to ""
            }
            val color = ContextCompat.getColor(this@HorizontalModeActivity, option.first)
            container.colorAnimation(lastColor, color)
            lastColor = color

            title.text = option.second
        }


        if (savedInstanceState == null) {
//            menu.showBadge(R.id.main)
//            menu.showBadge(R.id.data, 32)
        }
    }
}
