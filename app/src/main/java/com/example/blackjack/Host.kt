package com.example.blackjack

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.io.Serializable
import java.net.ServerSocket
import kotlin.collections.ArrayList


class Host(infoTextView: TextView){

    private val clientThread = HandlerThread("clientThread").apply { start() }
    private lateinit var networkController: NetworkController
    private var arrayClients = ArrayList<NetworkController>()
    private val connectedPlayers = ArrayList<String>()
    private val infoTextView : TextView
    private val handler = Handler(Looper.getMainLooper())
    private var currentPlayerIndex = 0
    private var playerTurnOrder: MutableList<NetworkController> = mutableListOf()


    init {
        this.infoTextView = infoTextView
    }


    fun runServer() {
        val server = ServerSocket(9999)
        while (true) {
            try {
                val client = server.accept()
                Log.d("CON", "client connected ${client.inetAddress.hostAddress}")
                val networkController = NetworkController(client,
                    Handler(
                        HandlerThread("clientThread: ${client.inetAddress}:${client.port}")
                            .apply { start() }.looper))
                arrayClients.add(networkController)
                networkController.onMessage = { message -> catchMessage(networkController, message) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startGame() {
        // Отправить сообщение "StartGame" всем клиентам
        for (controller in arrayClients.indices) {
            val message = if (controller == currentPlayerIndex) "YourTurn:" else "OpponentTurn:"
            arrayClients[controller].sendToHost(message) // Отправить сообщение "YourTurn" или "OpponentTurn" каждому клиенту

        }
        for (controller in arrayClients) {
            controller.sendToHost("StartGame:${currentPlayerIndex}")
        }
    }


    private fun catchMessage(networkController: NetworkController, message: String) {
        val parts = message.split(":")
        val check = parts[0]
        val content = parts[1]

        if (check == "PlayerReady") {
            connectedPlayers.add(content)
            updateConnectedPlayersTextView()
            playerTurnOrder.add(networkController)
        }
        if (check == "Enough") {
            if (networkController == playerTurnOrder[currentPlayerIndex]) {
                currentPlayerIndex++
                if (currentPlayerIndex >= playerTurnOrder.size) {
                    currentPlayerIndex = 0
                }
                playerTurnOrder[currentPlayerIndex].sendToHost("YourTurn")
            }
        }
    }

    private fun updateConnectedPlayersTextView() {
        val playersText = "С вами будут играть:\n${connectedPlayers.joinToString("\n")}"
        handler.post {
            infoTextView.text = playersText
            currentPlayerIndex++
        }
    }

    fun getCounterPlayers() : Int{
        return currentPlayerIndex
    }
}



