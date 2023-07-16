package com.example.blackjack

import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class DealerActivity : AppCompatActivity() {

    private lateinit var nickname : TextView
    private lateinit var ipAddress: TextView
    private lateinit var con: Host
    private var playerCount = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dealer)

        nickname = findViewById(R.id.username)
        val userName = intent.getStringExtra("username")
        nickname.text = userName

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip: String = Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        ipAddress = findViewById(R.id.ipAddress)
        ipAddress.text = ip

        val btnGiveCards: Button = findViewById(R.id.giveCards)

        btnGiveCards.setOnClickListener {
            playerCount = con.getCounterPlayers()

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("playerCount", playerCount)
            startActivity(intent)
            con.startGame()
        }
        con = Host(findViewById(R.id.infoTextView))
        Thread {
            con.runServer()}.start()
    }



}
