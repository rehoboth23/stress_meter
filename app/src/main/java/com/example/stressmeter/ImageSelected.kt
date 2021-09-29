package com.example.stressmeter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalTime

class ImageSelected : AppCompatActivity() {
    private var stressValue = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_image_selected)
        // get the image resource id from the bundle
        val bundle = intent.extras!!
        val resourceId = bundle.getInt("image_id", -1)
        stressValue = bundle.getInt("stress_value", -1)
        // set the image resource using the resource id
        val image = findViewById<ImageView>(R.id.selected_image)
        image.setImageResource(resourceId)
    }

    fun onCancel(v: View) {
        Log.d("$v.id", "clicked")
        finish()
    }

    fun onSubmit(v: View) {
        Log.d("$v.id", "clicked")
        // get date stamp data
        val date = LocalDate.now()
        val time = LocalTime.now()
        val year = date.year
        val month = date.monthValue
        val dayOfMonth = date.dayOfMonth
        val hour = time.hour
        val minute = time.minute
        // build string data for the post
        val dateStamp = "$month/$dayOfMonth/$year  $hour:$minute"
        val data = arrayOf(dateStamp, "$stressValue".trim())
        // use csv writer from utils to write to csv file @MainActivity.CSV_FILE_NAME
        csvWriter(applicationContext, CSV_FILE_NAME, data)
        finishAffinity()
    }
}