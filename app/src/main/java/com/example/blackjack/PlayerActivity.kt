package com.example.blackjack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class PlayerActivity : AppCompatActivity() {

    private lateinit var nickname : TextView
    private lateinit var conn : Client


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        nickname = findViewById(R.id.username)
        val userName = intent.getStringExtra("username")
        nickname.text = userName

        val ip = intent.getStringExtra("ip")

        conn = Client(this, userName.toString())
        Thread { conn.connectClient(ip.toString(), 9999)
            conn.sendToHost("MyNicknameIs", userName.toString())
        }.start()
    }
}