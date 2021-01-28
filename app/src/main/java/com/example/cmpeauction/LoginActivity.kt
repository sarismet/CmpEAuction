package com.example.cmpeauction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
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
    var address:String = ""
    val port = 8000
    var operation:String = "LOGIN"
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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