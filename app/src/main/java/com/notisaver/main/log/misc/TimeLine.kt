package com.notisaver.main.log.misc

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import com.notisaver.R
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

class TimeLine private constructor(private val context: Context) {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var Instance: TimeLine? = null


        @JvmStatic
        fun getInstance(context: Context): TimeLine {

            if (Instance == null) {
                synchronized(this) {
                    Instance = TimeLine(context.applicationContext)
                }
            }

            return Instance!!
        }

        internal const val ONE_SECOND: Long = 1000L
        internal const val ONE_MINUTE = 60 * ONE_SECOND
        internal const val ONE_HOUR = 60 * ONE_MINUTE
        internal const val ONE_DAY = 24 * ONE_HOUR
        internal const val ONE_WEEK = 7 * ONE_DAY
    }

    // 00:00 of day
    private var timeStartToDay: Long = System.currentTimeMillis()

    private val dateFormat = DateFormat.getMediumDateFormat(context)

    private val timeFormat = DateFormat.getTimeFormat(context)

    internal fun resetTodayTime() {
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        timeStartToDay = calendar.timeInMillis
    }

    internal fun formatTime(time: Long): String {
        return if (timeStartToDay < time) {
            timeFormat.format(time) + ", " + context.getString(R.string.today)
        } else {
            timeFormat.format(time) + " " + dateFormat.format(time)
        }
    }

    internal fun distanceFormatTime(time: Long): String {
        val distance = abs(time - System.currentTimeMillis())

        return if (distance < ONE_DAY) {
            val numberOfHours = ((distance * 1f)/ ONE_HOUR).roundToInt()
            "${numberOfHours}h"
        } else {
            val numberOfDay = ((distance * 1f)/ ONE_DAY).roundToInt()

            "${numberOfDay}d"
        }
    }

    internal fun getStartTime() = timeStartToDay
}