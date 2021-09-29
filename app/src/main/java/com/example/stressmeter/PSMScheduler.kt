package com.example.stressmeter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*


fun setSchedule(context: Context) {
    setSchedule(context, 12)
    setSchedule(context, 18)
}
private fun setSchedule(context: Context, hour: Int) {
    // the request code distinguish different stress   schedule instances
    val requestCode = hour * 10000
    val intent = Intent(context, EMAAlarmReceiver::class.java)
    //set pending intent to call EMAAlarmReceiver.
    val pi = PendingIntent.getBroadcast(context, requestCode, intent,
        PendingIntent.FLAG_IMMUTABLE)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    calendar[Calendar.HOUR_OF_DAY] = hour
    calendar[Calendar.MINUTE] = 30
    calendar[Calendar.SECOND] = 0
    if (calendar.timeInMillis < System.currentTimeMillis()) {
        calendar.add(Calendar.DATE, 1)
    }
    //set repeating alarm, and pass the pending intent,
    //so that the broadcast is sent every time the alarm
    // is triggered
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
}
