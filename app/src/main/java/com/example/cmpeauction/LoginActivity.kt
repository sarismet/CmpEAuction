package com.example.cmpeauction

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    //var address:String = "3.138.200.224"
    var address:String = "192.168.1.33"
    val port = 8000
    var operation:String = "LOGIN"
        private set
    var dialog:Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        dialog = Dialog(this)
    }

    fun winAlert() {

        dialog!!.setContentView(R.layout.win_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var imageViewClose:ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        var btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)

        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })

        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun loseAlert() {

        dialog!!.setContentView(R.layout.lose_layout)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var imageViewClose:ImageView = this.dialog!!.findViewById<ImageView>(R.id.ImageViewClose)
        var btnOk:Button = this.dialog!!.findViewById<Button>(R.id.btnOK)

        imageViewClose.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })

        btnOk.setOnClickListener(View.OnClickListener () {
            dialog!!.dismiss()
        })
        dialog!!.show()
    }

    fun login(view: View){
        val email = findViewById<EditText>(R.id.login_email).text.toString()
        val password = findViewById<EditText>(R.id.login_password).text.toString()
        var userInfos:String = "NULL"
            thread {
                try{
                    val map = HashMap<String, String>()
                    map.put("OPERATION",this.operation);
                    val map2 = HashMap<String, String>()
                    map2.put("EMAIL",email);
                    map2.put("PASSWORD",password);
                    val py:String = JSONObject(map2 as Map<*, *>).toString()
                    map.put("PAYLOAD",py);
                    val msg:String = JSONObject(map as Map<*, *>).toString()
                    val connection: Socket = Socket(address, port)
                    val writer: OutputStream = connection.getOutputStream()
                    writer.write((msg + '\n').toByteArray(Charset.defaultCharset()))
                    val readedData = connection.getInputStream().bufferedReader().readLine();
                    System.out.println("readedData is "+readedData)
                    var user: JSONObject = JSONObject(readedData)
                    userInfos = user.getString("user")

                }catch (e: Exception){
                    System.out.println(" Probably NULL is returned")
                }
            }.join()
            if (userInfos != "NULL") {
                val intent = Intent(this, AuctionProcessActivity::class.java)
                intent.putExtra("userInfos",userInfos)
                startActivity(intent)
            }else{
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
    }
    fun activateSignUp(view: View){
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}