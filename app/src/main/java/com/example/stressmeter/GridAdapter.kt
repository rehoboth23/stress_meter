package com.example.stressmeter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

@SuppressLint("InflateParams", "ViewHolder")
class GridAdapter(private val mContext: Context, private val data: IntArray) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return data[position].toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.layout_grid_cell, null)
        val gridImage = view.findViewById<ImageView>(R.id.grid_image)
        gridImage.setImageResource(getItemId(position).toInt())
        return view
    }
}