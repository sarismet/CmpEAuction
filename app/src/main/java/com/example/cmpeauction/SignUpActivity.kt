package com.example.cmpeauction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread

class SignUpActivity : AppCompatActivity() {
    val address = "192.168.1.38"
    val port = 22
    var operation:String = "SIGN_UP"
        private set
    var payload:String = ""
        private set
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
    }

    fun sign_up(view: View){
        val email = findViewById<EditText>(R.id.signup_email).text.toString()
        val username_sign_up = findViewById<EditText>(R.id.username).text.toString()
        val password1 = findViewById<EditText>(R.id.signup_password1).text.toString()
        val password2 = findViewById<EditText>(R.id.signup_password2).text.toString()
        val telnumber = findViewById<EditText>(R.id.telno).text.toString()

        if(password1==password2) {
            thread {
                try{
                    val map = HashMap<String, String>()
                    map.put("OPERATION",this.operation);
                    val map2 = HashMap<String, String>()
                    map2.put("EMAIL",email);
                    map2.put("USERNAME",username_sign_up);
                    map2.put("PASSWORD",password1);
                    map2.put("TELNO",telnumber);
                    val py:String = JSONObject(map2 as Map<*, *>).toString()
                    map.put("PAYLOAD",py);
                    val msg:String = JSONObject(map as Map<*, *>).toString()
                    System.out.println("msg is  " + msg)
                    val connection: Socket = Socket(address, port)
                    val writer: OutputStream = connection.getOutputStream()

                    writer.write((msg + '\n').toByteArray(Charset.defaultCharset()))
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
}