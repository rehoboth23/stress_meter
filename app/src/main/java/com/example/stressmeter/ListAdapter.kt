package com.example.stressmeter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.ArrayList

class ListAdapter(
    ctx: Context, private val resource: Int,
    private val rows: ArrayList<Array<String>>) : ArrayAdapter<Array<String>>(ctx, resource, rows) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val rowData = rows[position]
        @SuppressLint("ViewHolder") val row = inflater.inflate(resource, parent, false)
        // set row time stamp
        val timeStamp = row.findViewById<TextView>(R.id.time_stamp)
        timeStamp.text = rowData[0].trim { it <= ' ' }
        // set row stress value
        val stressValue = row.findViewById<TextView>(R.id.stress_level)
        stressValue.text = rowData[1].trim { it <= ' ' }
        // return the row view
        return row
    }
}