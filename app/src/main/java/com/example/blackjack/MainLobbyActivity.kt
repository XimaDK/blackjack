package com.example.blackjack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class MainLobbyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_lobby)

        val btnDealer: Button = findViewById(R.id.dealer)
        btnDealer.setOnClickListener{
            val userName = findViewById<EditText>(R.id.username)
            val dealerLayout = Intent(this, DealerActivity::class.java)
            dealerLayout.putExtra("username", userName.text.toString())
            startActivity(dealerLayout)
        }

        val btnPlayer : Button = findViewById(R.id.player)
        btnPlayer.setOnClickListener{
            val userName = findViewById<EditText>(R.id.username)
            val ip = findViewById<EditText>(R.id.ipAddress)
            val playerLayout = Intent(this, PlayerActivity::class.java)
            playerLayout.putExtra("username", userName.text.toString())
            playerLayout.putExtra("ip", ip.text.toString())
            startActivity(playerLayout)
        }

    }
}