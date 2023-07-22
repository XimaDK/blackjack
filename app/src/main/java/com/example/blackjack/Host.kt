package com.example.blackjack

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.io.Serializable
import java.net.ServerSocket
import kotlin.collections.ArrayList


class Host(infoTextView: TextView, username : String) : Serializable {


    private var arrayClients = ArrayList<NetworkController>()
    private val connectedPlayers = ArrayList<String>()
    private val infoTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var currentPlayerIndex = 0
    private var playerTurnOrder: MutableList<NetworkController> = mutableListOf()
    private var gameListener: GameListener? = null
    private var username : String



    init {
        this.infoTextView = infoTextView
        this.username = username
    }

    fun setTurnListener(listener: GameListener) {
        gameListener = listener
    }

    fun runServer() {
        val server = ServerSocket(9999)
        while (true) {
            try {
                val client = server.accept()
                Log.d("CON", "client connected ${client.inetAddress.hostAddress}")
                val networkController = NetworkController(
                    client,
                    Handler(
                        HandlerThread("clientThread: ${client.inetAddress}:${client.port}")
                            .apply { start() }.looper
                    )
                )
                arrayClients.add(networkController)
                networkController.onMessage =
                    { message -> catchMessage(networkController, message) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startGame() {
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
        if (check == "YourTurn") {
            val nextPlayer = parts[1].toInt()
            passTurnToNextPlayer(nextPlayer)
            gameListener?.onPlayerTurn() // Вызываем колбэк для обновления интерфейса игрока

        }
        if (check == "PlayerCardCount"){
            val dealerCardCount = content.toInt()
            // Обновите интерфейс других игроков, передав количество карт дилера
            gameListener?.playerCardCount(dealerCardCount)
        }
        if (check == "RevealCards") {
            val playerIndex = parts[2].toInt()
            val cards = content.split(",")
            val playerCards = cards.map { cardString ->
                val (rankStr, suitStr) = cardString.split("_")
                val rank = Rank.valueOf(rankStr.trim()) // Удаляем лишние пробелы из rankStr
                val suit = Suit.valueOf(suitStr.trim())
                Card(suit, rank)
            }

//            imageCardsPlayer(playerCards, playerIndex)
            gameListener?.revealPlayerCards(playerCards, playerIndex)
        }

        if (check == "InfoMessage"){
            gameListener?.setTextInfoView(content)
            sendMessageToClients(content)
        }
    }

    fun passTurnToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % playerTurnOrder.size
        val nextPlayer = arrayClients[currentPlayerIndex]
        nextPlayer.sendToHost("YourTurn:${currentPlayerIndex}:${playerTurnOrder.size}")
        gameListener?.onOpponentTurn()
        val hand : Hand = gameListener?.getHand()!!
        val playerCards = hand.getCards()
        imageCardsPlayer(playerCards, currentPlayerIndex)
    }

        private fun imageCardsPlayer(playerCards: List<Card>, currentPlayerIndex : Int){
        val strCards = playerCards.joinToString(",") { it.toString() }
        for (controller in arrayClients){

            controller.sendToHost("RevealCards:${strCards}:${currentPlayerIndex}")
        }
    }

    private fun passTurnToNextPlayer(nextPlayerIndex : Int){
        val nextPlayer = arrayClients[nextPlayerIndex]
        nextPlayer.sendToHost("YourTurn:${currentPlayerIndex}:${playerTurnOrder.size}")
        gameListener?.onOpponentTurn()
    }


    private fun updateConnectedPlayersTextView() {
        val playersText = "С вами будут играть:\n${connectedPlayers.joinToString("\n")}"
        handler.post {
            infoTextView.text = playersText
            currentPlayerIndex++
        }
    }

    fun getCounterPlayers(): Int {
        return currentPlayerIndex
    }

    fun updateDealerCardCount(count: Int) {
        val message = "PlayerCardCount:$count"
        for (controller in arrayClients) {
            controller.sendToHost(message)
        }
    }
    fun getUsername() : String{
        return username
    }


    fun sendMessageToClients(message: String) {
        for (controller in arrayClients) {
            controller.sendToHost("InfoMessage:$message")
        }
        gameListener?.setTextInfoView(message)
    }
}
