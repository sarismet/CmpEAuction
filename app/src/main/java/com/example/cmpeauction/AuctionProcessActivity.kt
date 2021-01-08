package com.example.cmpeauction

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*


class AuctionProcessActivity : AppCompatActivity() {
    var COUNT_DOWN:Long = 1000000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_process)
        count_down();
    }

    fun count_down(){
        val mTextViewCountDown = findViewById<TextView>(R.id.text_count_down)
        object : CountDownTimer(COUNT_DOWN, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = ((millisUntilFinished / 1000) / 60).toString()
                val seconds = ((millisUntilFinished / 1000) % 60).toString()
                val time_left =  minutes + ":" + seconds
                mTextViewCountDown.setText(time_left)
            }
            override fun onFinish() {
                mTextViewCountDown.setText("done!")
            }
        }.start()
    }


}