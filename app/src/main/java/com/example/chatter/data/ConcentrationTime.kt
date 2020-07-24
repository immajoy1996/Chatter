package com.example.chatter.data

class ConcentrationTime(private var minute: Int, private var second: Int) {
    fun addOneSecond() {
        if (second == 59) {
            second = 0
            minute++
        } else {
            second++
        }
    }

    fun getFormattedTime(): String {
        var minuteString = minute.toString()
        var secondString = second.toString()
        if (minute < 10) {
            minuteString = "0$minuteString"
        }
        if (second < 10) {
            secondString = "0$secondString"
        }
        return "$minuteString:$secondString"
    }
}