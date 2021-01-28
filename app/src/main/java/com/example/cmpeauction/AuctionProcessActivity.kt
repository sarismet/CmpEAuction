package com.example.cmpeauction

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class Product(link: String, name: String, price: Int, time: String) {
    var link:String = link
    var name:String = name
    var price:Int = price
    var time:String = time
}

class User(username: String, id: Int, balance: Int) {
    var username:String = username
    var id:Int = id
    var balance:Int = balance
}

class AuctionProcessActivity : AppCompatActivity() {
    var COUNT_DOWN:Long = 300000;
    var address:String = ""
    val port = 8000
    var operation:String = "SHOW"
        private set
    var product:Product? = null
    val time_left = 0
    var isTimeToBuy:Boolean = false
    var user:User? = null
    var connectionx: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_process)
        val userInfos= intent.getStringExtra("userInfos")
        var userInfosJson: JSONObject = JSONObject(userInfos)
        var username = userInfosJson.getString("username")
        var id = userInfosJson.getInt("id")
        var balance = userInfosJson.getInt("balance")
        user = User(username,id,balance);
        val buttonToBuy = findViewById<Button>(R.id.button)
        buttonToBuy.setOnClickListener {
            if (false) {
                if (product != null && user != null) {
                    if (user!!.balance.toInt() > product!!.price) {
                        thread {
                            val map = HashMap<String, String>()
                            map.put("OPERATION", "BUY");
                            val payloadMap = HashMap<String, String>()
                            payloadMap.put("USERNAME", user!!.username)
                            payloadMap.put("USERID", user!!.id.toString())
                            payloadMap.put("PRODUCTNAME", product!!.name)
                            val py: String = JSONObject(payloadMap as Map<*, *>).toString()
                            map.put("PAYLOAD", py);
                            val msg: String = JSONObject(map as Map<*, *>).toString()
                            val writer: OutputStream = connectionx!!.getOutputStream()
                            writer.write((msg + '\n').toByteArray(Charset.defaultCharset()))
                            val stringReader = connectionx!!.getInputStream().bufferedReader().readLine();
                            System.out.println("stringReader -> " + stringReader)
                            var userInfos: JSONObject = JSONObject(stringReader)
                            var buyer = userInfos.getString("buyer")
                            if (buyer == user!!.username) {
                                user!!.balance = user!!.balance - product!!.price
                                Toast.makeText(this, "Congratulations!!! You have bought this product", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Unfortunately this product was bought by$buyer", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else{
                    Toast.makeText(this, user!!.username, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "It is not time to buy this product wait until the timer show 05:00"+user!!.username, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun get_product(){
        try{
            val mapx = HashMap<String, String>()
            mapx.put("OPERATION", "SHOW");
            val sdf = SimpleDateFormat("HH:mm:ss")
            val currentDate:String = sdf.format(Date())
            mapx.put("PAYLOAD", currentDate);
            val msgx:String = JSONObject(mapx as Map<*, *>).toString()
            connectionx = Socket(address, port)
            val writerx: OutputStream = connectionx!!.getOutputStream()
            writerx.write((msgx + '\n').toByteArray(Charset.defaultCharset()))
            val stringReaderx = connectionx!!.getInputStream().bufferedReader().readLine();
            System.out.println("stringReaderx is in get product ->" + stringReaderx)
            var productx:JSONObject = JSONObject(stringReaderx)
            val s = productx.getString("products")
            val remainingTime = productx.getString("TIME")
            this.COUNT_DOWN = remainingTime.toLong()
            val gson = Gson()
            product = gson.fromJson(s, Product::class.java)
        }catch (e: Exception){
            System.out.println("Exception ->" + e)
        }
    }

    override fun onResume() {
        super.onResume()
        count_down()
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
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
                val mil: Long = (millisUntilFinished / 1000)
                if(mil<=300){
                    isTimeToBuy = true
                }
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

