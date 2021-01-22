package com.example.cmpeauction

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.json.JSONObject
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class Product(link: String, name: String, price: Int, owner: String) {
    var link:String = link
    var name:String = name
    var price:Int = price
    var owner:String = owner
}

class AuctionProcessActivity : AppCompatActivity() {
    var COUNT_DOWN:Long = 72000;
    var address:String = "192.168.1.41"
    val port = 22
    var operation:String = "SHOW"
        private set
    var product:Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_process)
        count_down();
    }

    fun get_product(){
        try{
            val mapx = HashMap<String, String>()
            mapx.put("OPERATION","SHOW");
            mapx.put("PAYLOAD","empty");
            val msgx:String = JSONObject(mapx as Map<*, *>).toString()
            val connectionx: Socket = Socket(address, port)
            val writerx: OutputStream = connectionx.getOutputStream()
            writerx.write((msgx + '\n').toByteArray(Charset.defaultCharset()))
            val stringReaderx = connectionx.getInputStream().bufferedReader().readLine();
            var productx:JSONObject = JSONObject(stringReaderx)
            val s = productx.getString("products")
            val gson = Gson()
            product = gson.fromJson(s,Product::class.java)
        }catch (e: Exception){
            System.out.println("Exception ->" + e)
        }
    }

    fun count_down(){

        val job = thread {
            get_product()
        }
        job.join()
        val myWebView: WebView = findViewById(R.id.webview)
        val html:String = "<html><body><img src=\"" + (product?.link ?: "") + "\" width=\"100%\" height=\"100%\"\"/></body></html>";
        myWebView.loadData(html, "text/html", null);
        val mTextViewCountDown = findViewById<TextView>(R.id.text_count_down)
        val price_Text: TextView = findViewById(R.id.text_price)
        price_Text.text = "Price : "+(product?.price ?: 0).toString()
        val name_Text: TextView = findViewById(R.id.name)
        name_Text.text = (product?.name ?: "")
        var count:Int = 30
        var price_product:Int = (product?.price ?: 0)
        object : CountDownTimer(COUNT_DOWN, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = ((millisUntilFinished / 1000) / 60).toString()
                val seconds = ((millisUntilFinished / 1000) % 60).toString()
                val time_left =  minutes + ":" + seconds
                mTextViewCountDown.setText(time_left)
                count--
                if(count == 0){
                    count = 30
                    price_product = price_product-15
                    price_Text.text = "Price : "+price_product.toString()
                }
            }
            override fun onFinish() {
                count_down()
            }
        }.start()

    }


}

