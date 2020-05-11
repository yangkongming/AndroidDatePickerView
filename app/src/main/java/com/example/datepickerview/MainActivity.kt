package com.example.datepickerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var customDatePicker: CustomDatePicker? = null
    private var now = ""
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    var timer: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timer = findViewById<TextView>(R.id.time)

        now = sdf.format(Date())
        customDatePicker = CustomDatePicker(this, CustomDatePicker.ResultHandler { time ->

            timer?.text = time
        }, "2020-05-01 00:00", now)
        customDatePicker?.setIsLoop(false)
        customDatePicker?.show(now)

        timer?.setOnClickListener {
            customDatePicker?.show(now)
        }
    }
}
