package com.example.cmpeauction

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import org.json.JSONObject
import java.io.OutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.Socket
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

class Product(link: String, name: String, price: Double, time: String, soldTo: String) {
    var link:String = link
    var name:String = name
    var price:Double = price
    var time:String = time
    var soldTo:String = soldTo
}

class User(username: String, id: Int, balance: Double, uuid:String) {
    var username:String = username
    var id:Int = id
    var balance:Double = balance
    var uuid:String = uuid
}

class AuctionProcessActivity : AppCompatActivity() {
    var COUNT_DOWN:Long = 60000;
    //var address:String = "3.138.200.224"
    var address:String = "192.168.1.33"
    val port = 8000
    var operation:String = "SHOW"
        private set
    var product:Product? = null
    val time_left = 0
    var isTimeToBuy:Boolean = false
    var user:User? = null
    var connectionx: Socket? = null
    var dialog:Dialog? = null
    var max_price:Double = 0.0
    var remainingT:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auction_process)
        val userInfos= intent.getStringExtra("userInfos")
        val userInfosJson: JSONObject = JSONObject(userInfos)
        val username = userInfosJson.getString("username")
        val id = userInfosJson.getInt("id")
        val balance = userInfosJson.getDouble("balance")
        val uuid = userInfosJson.getString("uuid")
        user = User(username,id,balance,uuid);
        this.dialog = Dialog(this)
        this.max_price = 0.0
    }

    fun buyAction() {
        val buttonToBuy = findViewById<Button>(R.id.buttonToBuy)
        buttonToBuy.setOnClickListener {
            System.out.println("isTimeToBuy -> " + isTimeToBuy)
            if (isTimeToBuy) {
                System.out.println("product  user -> " + product!!.soldTo+" - "+user!!.username)
                if (product != null && user != null) {
                    if (user!!.balance > product!!.price) {
                        if (product!!.soldTo.equals("NULL")) {
                            var buyer:String = ""
                            val job2 = thread {
                                try{
                                    val map = HashMap<String, String>()
                                    map.put("OPERATION", "BUY");
                                    val payloadMap = HashMap<String, String>()
                                    payloadMap.put("USERNAME", user!!.username)
                                    payloadMap.put("USERID", user!!.id.toString())
                                    payloadMap.put("PRODUCTNAME", product!!.name)
                                    payloadMap.put("PRODUCTPRICE", product!!.price.toString())
                                    val py: String = JSONObject(payloadMap as Map<*, *>).toString()
                                    map.put("PAYLOAD", py);
                                    var connectionbuy = Socket(address, port)
                                    val msg: String = JSONObject(map as Map<*, *>).toString()
                                    val writer: OutputStream = connectionbuy.getOutputStream()
                                    System.out.println("msg -> " + msg)
                                    writer.write((msg + '\n').toByteArray(Charset.defaultCharset()))
                                    val stringReader = connectionbuy.getInputStream().bufferedReader().readLine();
                                    System.out.println("stringReader -> " + stringReader)
                                    var userInfos: JSONObject = JSONObject(stringReader)
                                    buyer = userInfos.getString("buyer")
                                    Log.d("BUYYER",buyer)
                                    connectionbuy.close()
                                }catch (e: Exception){
                                    System.out.println("Exception ->" + e)
                                }
                            }
                            job2.join()
                            if (buyer.equals(user!!.username)) {
                                user!!.balance = user!!.balance - product!!.price
                                product!!.soldTo = user!!.username
                                winAlert()
                            }
                            else if(buyer.equals("NOT_ENOUGH")){
                                notEnoughAlert()
                            }
                            else {
                                loseAlert(buyer)
                            }
                        }else{
                            waitAlert(product!!.soldTo)
                        }
                    }else{
                        notEnoughAlert()
                    }
                }else{
                    Toast.makeText(this, user!!.username, Toast.LENGTH_SHORT).show()
                }
            } else {
                remainingAlert(this.remainingT-60000)
            }
        }
    }

    fun notEnoughAlert() {
        dialog!!.setContentView(R.layout.not_enough_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imageViewClose: ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        val btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)
        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun winAlert() {
        System.out.println("Win Alert is called")
        dialog!!.setContentView(R.layout.win_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imageViewClose: ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        val btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)
        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun waitAlert(buyyerName:String) {
        System.out.println("Win Alert is called")
        dialog!!.setContentView(R.layout.wait_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imageViewClose: ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        val btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)
        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        val waitText: TextView = this.dialog!!.findViewById<TextView>(R.id.waitText)
        waitText.text = "The product is bought by "+buyyerName
        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun remainingAlert(remainingTime: Long) {
        dialog!!.setContentView(R.layout.clock_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val imageViewClose: ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        val btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)
        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        val waitText: TextView = this.dialog!!.findViewById<TextView>(R.id.NextSaleTime)
        object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = ((millisUntilFinished / 1000) / 60).toString()
                val seconds = ((millisUntilFinished / 1000) % 60).toString()
                val time_left =  minutes + ":" + seconds
                waitText.setText(time_left)
            }
            override fun onFinish() {
                Toast.makeText(this@AuctionProcessActivity, "Now, You can buy it", Toast.LENGTH_SHORT).show()
            }
        }.start()
        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun loseAlert(buyyerName:String) {
        dialog!!.setContentView(R.layout.lose_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        var imageViewClose: ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        var btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)
        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        val loserText: TextView = this.dialog!!.findViewById<TextView>(R.id.loserText)
        if(buyyerName==user!!.username) {
            loserText.text = "You already bought this"
        }
        else{
            loserText.text = "The product is bought by "+buyyerName
        }
        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun get_product(){
        try{
            val mapx = HashMap<String, String>()
            mapx.put("OPERATION", "SHOW");
            mapx.put("UUID", this.user!!.uuid);
            val msgx:String = JSONObject(mapx as Map<*, *>).toString()
            this.connectionx = Socket(address, port)
            val writerx: OutputStream = connectionx!!.getOutputStream()
            writerx.write((msgx + '\n').toByteArray(Charset.defaultCharset()))
            Log.d("msgx",msgx)
            val stringReaderx = connectionx!!.getInputStream().bufferedReader().readLine();
            System.out.println("stringReaderx is in get product ->" + stringReaderx)
            var productx:JSONObject = JSONObject(stringReaderx)
            val s = productx.getString("products")
            val remainingTime = productx.getString("TIME")
            this.COUNT_DOWN = remainingTime.toLong()
            val gson = Gson()
            product = gson.fromJson(s, Product::class.java)
            this.isTimeToBuy = false
            this.max_price = product!!.price
            var fullTime: Double = 60000.0
            Log.d("CGOT", COUNT_DOWN.toString())
            if(this.COUNT_DOWN<fullTime){
                var div = this.COUNT_DOWN/10000.0
                var diff = div-div.toInt()
                Log.d("diff", diff.toString())
                var part = 0
                if(diff>0.0){
                    part = div.toInt()+1
                }
                Log.d("part", part.toString())
                this.product!!.price = ((10-(6-part))*product!!.price/10.0)
                Log.d("newprice", this.product!!.price.toString())
            }
            this.connectionx!!.close()
        }catch (e: Exception){
            System.out.println("Exception ->" + e)
        }
    }
    override fun onResume() {
        super.onResume()
        buyAction()
        count_down()
    }
    fun count_down(){
        Log.d("count_down","count_down is invoked")
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
        var price_product:Double = (product?.price ?: 0.0)
        Log.d("CPLAY ",COUNT_DOWN.toString())
        object : CountDownTimer(COUNT_DOWN, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = ((millisUntilFinished / 1000) / 60).toString()
                val seconds = ((millisUntilFinished / 1000) % 60).toString()
                remainingT=millisUntilFinished
                val sec = ((millisUntilFinished / 1000) % 60)

                var div = millisUntilFinished/10000.0
                var diff = div-div.toInt()

                val time_left =  minutes + ":" + seconds
                mTextViewCountDown.setText(time_left)
                price_Text.text = "Price : "+price_product.toString()

                if(minutes.equals("0")){
                    Log.d("diff ",diff.toString()+" - sec - "+sec)
                    isTimeToBuy = true
                    if(diff<0.1&&product!!.soldTo.equals("NULL")){
                        price_product = price_product-max_price/10.0
                        product!!.price = price_product
                    }
                }
            }
            override fun onFinish() {
                Log.d("onFinish ","onFinish is invoked")
                count_down()
            }
        }.start()

    }
}

