package com.example.blackjack

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class Client(private val context: Context,
            nickname: String) {

    private lateinit var client: Socket
    private lateinit var input: BufferedReader
    private lateinit var output: PrintWriter
    private lateinit var handler: Handler
    private var nickname: String
    private val dealerHand = Hand()
    private val playerHand = Hand()

    init {
        this.nickname = nickname
    }


    fun connectClient(ip: String, PORT: Int) {
        handler = Handler(Looper.getMainLooper())
        //ПОМЕНЯТЬ НА ip потом
        client = Socket("192.168.2.137", PORT)
        input = BufferedReader(InputStreamReader(client.inputStream))
        output = PrintWriter(client.getOutputStream(), true)
        startListening()
    }


    private fun startListening() {
        Thread {
            try {
                while (true) {
                    val message = input.readLine()
                    if (message != null) {
                        Log.d("CON", "Клиенту пришло сообщение: $message")
                        catchMessage(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendToHost(message: String, nickname: String) {
        val fullMessage = "$message:$nickname"
        Thread {
            output.println(fullMessage)
        }.start()
    }

    private fun catchMessage(message: String) {
        if (message == "StartGame") {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("dealerHand", dealerHand)
            intent.putExtra("playerHand", playerHand)
            context.startActivity(intent)
            sendToHost("RequestDealerCards", nickname)
        }

        if (message.startsWith("DealerCards")) {
            val dealerCardsString = message.substringAfter(":")
            val dealerCards = dealerCardsString.split(",").map { CardUtils.createCardFromString(it) }

            val gameActivity = context as GameActivity
            gameActivity.runOnUiThread {
                gameActivity.updateDealerCards(dealerCards)
            }
        } else if (message.startsWith("PlayerCards")) {
            val playerCardsString = message.substringAfter(":")
            val playerCards = playerCardsString.split(",").map { CardUtils.createCardFromString(it) }

            val gameActivity = context as GameActivity
            gameActivity.runOnUiThread {
                gameActivity.updatePlayerCards(playerCards)
            }
        }
    }
}
