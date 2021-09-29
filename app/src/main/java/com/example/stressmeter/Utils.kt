package com.example.stressmeter

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils.join
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.util.ArrayList

@RequiresApi(Build.VERSION_CODES.S)
fun checkPermission(activity: Activity) {

    // check if app has permission to write to external storage and use camera and read external storage
    if (ContextCompat.checkSelfPermission(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
    ) // request permission to write to external storage and use camera and read external storage if not grated
        ActivityCompat.requestPermissions(
            activity, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 0
        )

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SCHEDULE_EXACT_ALARM)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM), 0)
    }

    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.VIBRATE)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.VIBRATE), 0)
    }
}

fun csvReader(context: Context, fileName: String): ArrayList<Array<String>> {
    val history = ArrayList<Array<String>>()
    val csvFile = File(context.getExternalFilesDir(null), fileName)
    val csvFileReader = FileReader(csvFile)
    val reader = BufferedReader(csvFileReader)
    var line: String? = reader.readLine()
    // read line; delineate them at ","; add to history list
    while (line != null && line.isNotEmpty()) {
        val row = line.trim().split(",").toTypedArray()
        history.add(row)
        line = reader.readLine()
    }
    // close readers
    csvFileReader.close()
    reader.close()
    return history
}

fun csvWriter(context: Context, fileName: String, data: Array<String>) {
    // get file writer stream set up
    val file = File(context.getExternalFilesDir(null).toString() + "/" + fileName)
    val newLine: String

    newLine = if (file.createNewFile()) {
        data.joinToString(",")
    } else {
        "${file.readText()}\n${data.joinToString(",")}"
    }

    file.writeText(newLine)
}