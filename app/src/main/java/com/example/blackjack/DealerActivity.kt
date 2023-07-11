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
    private lateinit var dealerHand: Hand
    private lateinit var playerHand: Hand
    private lateinit var deck : Deck
    private lateinit var con: Host



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

        deck = Deck(this)
        dealerHand = Hand()
        dealerHand.addCards(deck.dealCards(2))
        playerHand = Hand()
        playerHand.addCards(deck.dealCards(2))
        val btnGiveCards: Button = findViewById(R.id.giveCards)

        btnGiveCards.setOnClickListener {
            con.startGame(dealerHand, playerHand)

            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("dealerHand", dealerHand)
            intent.putExtra("playerHand", playerHand)
            startActivity(intent)
        }


        con = Host(findViewById(R.id.infoTextView))
        Thread {
            con.runServer()}.start()

    }

}