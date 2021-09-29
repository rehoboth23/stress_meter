package com.example.stressmeter

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.stressmeter.PSM.getGridById
import com.google.android.material.navigation.NavigationView
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView
import java.util.*

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("InflateParams")
class MainActivity : AppCompatActivity() {
    private var grid = 0
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var preferences: SharedPreferences
    private lateinit var r: Ringtone
    private lateinit var vib: Vibrator
    private var playing = false
    private lateinit var toggle: ActionBarDrawerToggle
    private var launchView = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // alarm
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        r = RingtoneManager.getRingtone(applicationContext, notification)
        r.isLooping = true
        vib = getSystemService(VIBRATOR_SERVICE) as Vibrator
        r.play()
        playing = true
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (playing) {
                    vib.vibrate(800)
                    mainHandler.postDelayed(this, 1000)
                }
            }
        })

        val inflater = LayoutInflater.from(applicationContext)
        val main = inflater.inflate(R.layout.activity_main, null)
        preferences = getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE)

        // task new alarms and cancel old alarms
        setSchedule(applicationContext)

        setContentView(main)
        checkPermission(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        launchView = preferences.getInt("launch_view", 1)
        setMainView()

        // set toggle for the nav drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        toggle.syncState()

        drawerLayout.addDrawerListener(toggle)

        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView = findViewById<NavigationView>(R.id.nav_view)

        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { item ->
            val edit: SharedPreferences.Editor = preferences.edit()
            if (r.isPlaying) {
                r.stop()
            }
            playing = false
            launchView = when (item.itemId) {
                R.id.menu_item_1 -> 1
                R.id.menu_item_2 -> 2
                else -> {
                    Log.d("input issues", "invalid input")
                    return@OnNavigationItemSelectedListener false
                }
            }
            println("outside")
            edit.apply()
            setMainView()
            true
        })
    }

    override fun onStop() {
        val editor = preferences.edit()
        editor.remove("launch_view")
        editor.apply()
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)
    }

    private fun makeGrid() {
        // instantiate adapter
        val gridAdapter = GridAdapter(this, getGridById(grid))
        // instantiate grid view and set the adapter
        val gridView = findViewById<GridView>(R.id.grid_view)
        gridView.adapter = gridAdapter

        // set the item click listener
        gridView.onItemClickListener =
            OnItemClickListener { _, _, position, _ ->
                if (r.isPlaying) {
                    r.stop()
                }
                playing = false

                // make an intent for the selected image
                val imageSelectedIntent = Intent(applicationContext, ImageSelected::class.java)
                val bundle = Bundle()

                // put the bundle into the intent
                bundle.putInt("image_id", gridAdapter.getItemId(position).toInt())
                bundle.putInt("stress_value", position)
                imageSelectedIntent.putExtras(bundle)

                // start the intent
                startActivity(imageSelectedIntent)
            }
    }

    private fun setMainView() {
        // remove previous view
        val prev = drawerLayout.findViewById<LinearLayout>(R.id.settable)
        val index: Int = drawerLayout.indexOfChild(prev)
        drawerLayout.removeViewAt(index)

        // programmatically  set content view
        if (launchView == 1) {
            // get view
            val inflater = LayoutInflater.from(applicationContext)
            val view = inflater.inflate(R.layout.layout_home, null)
            view.findViewById<View>(R.id.more_images).setOnClickListener {
                if (r.isPlaying) {
                    r.stop()
                }
                playing = false
                moreImages()
            }
            // put the layout into the drawer layout
            drawerLayout.addView(view, index)
            // start grid at grid 1
            grid = 1
            makeGrid()
        } else {
            val inflater = LayoutInflater.from(applicationContext)
            val view = inflater.inflate(R.layout.layout_stress_history, null)

            // make list adapter and get source data using utils csv reade
            val rows: ArrayList<Array<String>> = csvReader(applicationContext, CSV_FILE_NAME)
            val listAdapter = ListAdapter(applicationContext, R.layout.layout_history_row, rows)

            // get list view and set adapter
            val listView = view.findViewById<ListView>(R.id.history_sheet)
            listView.adapter = listAdapter

            // get the chart
            val chart: LineChartView = view.findViewById(R.id.chart)
            chart.isInteractive = true // set the chart to be interactive
            chart.zoomType = ZoomType.HORIZONTAL_AND_VERTICAL // set to uniform zoom
            // enable the horizontal scroll
            chart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
            // enable the vertical scroll
            chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL)

            // make the points for the graph
            val pointValues = ArrayList<PointValue>()
            for (i in rows.indices) {
                val row = rows[i]
                val pointValue = PointValue(i.toFloat(), row[1].toInt().toFloat())
                pointValues.add(pointValue)
            }

            // set the lines
            val line = Line(pointValues).setColor(Color.BLUE).setCubic(true)
            line.strokeWidth = 1
            val lines = ArrayList<Line>()
            lines.add(line)
            line.isFilled = true


            // set the line chart data using lines
            val data = LineChartData()
            data.lines = lines
            // make the axis
            val yValues = ArrayList<Float>()
            yValues.add(4f)
            yValues.add(6f)
            yValues.add(8f)
            yValues.add(10f)
            yValues.add(12f)
            yValues.add(14f)
            val axisX = Axis()
            val axisY = Axis.generateAxisFromCollection(yValues)

            // set the view port
            val viewport = Viewport(0F, 14F, 0F, 1F)
            chart.currentViewport = viewport

            // set the axis name
            axisX.name = "Instances"
            axisY.name = "Stress Level"

            // set the axis
            data.axisXBottom = axisX
            data.axisYLeft = axisY

            // set the line chat view using data
            chart.lineChartData = data

            // add view to drawer layout
            drawerLayout.addView(view, index)
        }
        drawerLayout.closeDrawers()
    }

    private fun moreImages() {
        if (grid < 3) grid += 1 else grid = 1
        makeGrid()
    }
}