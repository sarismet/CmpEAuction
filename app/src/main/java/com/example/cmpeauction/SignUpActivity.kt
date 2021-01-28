package com.example.cmpeauction

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import org.json.JSONObject
import java.io.OutputStream
import java.net.Socket
import java.nio.charset.Charset
import kotlin.collections.HashMap
import kotlin.concurrent.thread


class SignUpActivity : AppCompatActivity() {
    var address:String = ""
    val port = 8000
    var operation:String = "SIGN_UP"
        private set
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var intent2 = getIntent()
        val dataToSend2= intent2.getStringExtra("sa")
        System.out.println("dataToSend ın create is "+dataToSend2)
    }

    fun sign_up(view: View){
        val email = findViewById<EditText>(R.id.signup_email).text.toString()
        val username_sign_up = findViewById<EditText>(R.id.username).text.toString()
        val password1 = findViewById<EditText>(R.id.signup_password1).text.toString()
        val password2 = findViewById<EditText>(R.id.signup_password2).text.toString()
        val telnumber = findViewById<EditText>(R.id.telno).text.toString()

        if(password1==password2) {
            var userModelString:String = "NULL"
            thread {
                try{
                    val map = HashMap<String, String>()
                    map.put("OPERATION", this.operation);
                    val map2 = HashMap<String, String>()
                    map2.put("EMAIL", email);
                    map2.put("USERNAME", username_sign_up);
                    map2.put("PASSWORD", password1);
                    map2.put("TELNO", telnumber);
                    val py:String = JSONObject(map2 as Map<*, *>).toString()
                    map.put("PAYLOAD", py);
                    val msg:String = JSONObject(map as Map<*, *>).toString()
                    System.out.println("msg is  " + msg)
                    val connection: Socket = Socket(address, port)
                    val writer: OutputStream = connection.getOutputStream()
                    writer.write((msg + '\n').toByteArray(Charset.defaultCharset()))
                    val stringReader = connection.getInputStream().bufferedReader().readLine();
                    System.out.println("stringReader is ->"+stringReader)
                    var userInfos:JSONObject = JSONObject(stringReader)
                    userModelString = userInfos.getString("user")
                    connection.close();
                }catch (e: Exception){
                    System.out.println(" Probably NULL is returned")
                }
            }.join()
            if(userModelString!="NULL"){
                val intent = Intent(this, AuctionProcessActivity::class.java)
                intent.putExtra("userInfos",userModelString)
                startActivity(intent)
            }
            else {
                Toast.makeText(this, "Sign Up is not successful userModelString is $userModelString ", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(this, "passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }
}