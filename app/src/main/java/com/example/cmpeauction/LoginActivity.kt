package com.example.cmpeauction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread


class LoginActivity : AppCompatActivity() {
    val address = "192.168.1.41"
    val port = 22
    var operation:String = "LOGIN"
        private set
    var payload:String = ""
        private set
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun login(view: View){
        val email = findViewById<EditText>(R.id.login_email).text.toString()
        val password = findViewById<EditText>(R.id.login_password).text.toString()
        if(email == "sarismet2825@gmail.com" && password == "asd") {
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
                    val reader: InputStream = connection.getInputStream()
                    val stringReader = connection.getInputStream().bufferedReader().readLine();
                }catch (e: Exception){
                    System.out.println("Exception ->" + e)
                }
            }
            val intent = Intent(this, AuctionProcessActivity::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    fun activateSignUp(view: View){
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}