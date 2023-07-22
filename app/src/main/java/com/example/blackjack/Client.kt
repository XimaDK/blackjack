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
    private var gameListener: GameListener? = null
    private var nextPlayerIndex  = 0
    private var maxPlayers = 0
    init {
        this.nickname = nickname
    }

    fun setTurnListener(listener: GameListener) {
        gameListener = listener
    }

    fun connectClient(ip: String, PORT: Int) {
        handler = Handler(Looper.getMainLooper())
        client = Socket(ip, PORT)
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

    fun sayReady(){
        sendToHost("PlayerReady", nickname)
        ConnectionWrapper.setClient(this)
    }

    fun sendToHost(message: String, nickname: String) {
        val fullMessage = "$message:$nickname"
        Thread {
            output.println(fullMessage)
        }.start()
    }


    private fun catchMessage(message: String) {
        val parts = message.split(":")
        val check = parts[0]
        val content = parts[1]
        if (check == "StartGame") {
            val intent = Intent(context, GameActivity::class.java)
            intent.putExtra("playerCount", content.toInt())
            context.startActivity(intent)
        }
        if (check == "YourTurn") {
            gameListener?.onPlayerTurn()
            nextPlayerIndex = content.toInt() + 1

        }
        if (check == "PlayerCardCount") {
            val dealerCardCount = content.toInt()
            gameListener?.playerCardCount(dealerCardCount)
        }

        if (check == "RevealCards") {
            val cards = content.split(",")
            val playerIndex = parts[2].toInt()
            val playerCards = cards.map { cardString ->
                val (rankStr, suitStr) = cardString.split("_")
                val rank = Rank.valueOf(rankStr)
                val suit = Suit.valueOf(suitStr)
                Card(suit, rank)
            }
            gameListener?.revealPlayerCards(playerCards, playerIndex)
        }

        if (check == "InfoMessage") {
            gameListener?.setTextInfoView(content)
        }
    }

    fun passTurnToNextPlayer() {
        if (nextPlayerIndex < maxPlayers){
            val message = "YourTurn:$nextPlayerIndex"
            sendToHost(message, nickname)
        }

        else{
            gameListener?.onOpponentTurn()
        }
        sendToHost("RevealCards:${gameListener?.getHand()}:${nextPlayerIndex}",nickname)
    }

    fun getNickname() : String{
        return nickname
    }



    fun updatePlayerCardCount(count: Int) {
        val message = "PlayerCardCount:$count"
        sendToHost(message, nickname)
    }
}
