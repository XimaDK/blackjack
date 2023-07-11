package com.example.blackjack

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.net.ServerSocket
import kotlin.collections.ArrayList


class Host(infoTextView: TextView){

    private val clientThread = HandlerThread("clientThread").apply { start() }
    private lateinit var networkController: NetworkController
    private var arrayClients = ArrayList<NetworkController>()
    private val connectedPlayers = ArrayList<String>()
    private val infoTextView : TextView
    private val handler = Handler(Looper.getMainLooper())
    private var dealerHand = Hand()
    private var playerHand = Hand()
    private lateinit var dealerCards : List<Card>


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

    fun startGame(dealerHand: Hand, playerHand: Hand) {
        dealerCards = dealerHand.getCards()
        val dealerCardsString = dealerCards.joinToString(",") { it.toString() }

        // Отправить сообщение "StartGame" всем клиентам
        for (controller in arrayClients) {
            controller.sendToHost("StartGame")
            controller.sendToHost("DealerCards:$dealerCardsString")
            controller.sendToHost("PlayerCards:${playerHand.getCards().joinToString(",") { it.toString() }}")
        }
    }


    private fun catchMessage(networkController: NetworkController, message: String) {
        val parts = message.split(":")
        val check = parts[0]
        val content = parts[1]

        if (parts.size == 2) {
            if (check == "MyNicknameIs") {
                connectedPlayers.add(content)
                updateConnectedPlayersTextView()
            }
        }

        if (check == "RequestDealerCards") {
            // Отправка карт дилера
            dealerCards = getDealerCards()
            val dealerCardsString = dealerCards.joinToString(",") { it.toString() }
            networkController.sendToHost("DealerCards:$dealerCardsString")
        }

        if (check == "RequestPlayerCards") {
            // Отправка карт игрока
            val playerCards = getPlayerCards()
            val playerCardsString = playerCards.joinToString(",") { it.toString() }
            networkController.sendToHost("PlayerCards:$playerCardsString")
        }

        // Добавьте следующую логику для передачи карт от клиента к хосту
        if (check == "DealerCards") {
            val dealerCards = content.split(",").map { CardUtils.createCardFromString(it) }
            setDealerCards(dealerCards)
            // Отправка полученных карт дилера всем клиентам
            for (controller in arrayClients) {
                controller.sendToHost("DealerCards:${dealerCards.joinToString(",") { it.toString() }}")
            }
        }

        if (check == "PlayerCards") {
            val playerCards = content.split(",").map { CardUtils.createCardFromString(it) }
            setPlayerCards(playerCards)
            // Отправка полученных карт игрока всем клиентам
            for (controller in arrayClients) {
                controller.sendToHost("PlayerCards:${playerCards.joinToString(",") { it.toString() }}")
            }
        }
    }

    private fun updateConnectedPlayersTextView() {
        val playersText = "С вами будут играть:\n${connectedPlayers.joinToString("\n")}"
        handler.post {
            infoTextView.text = playersText
        }
    }
    private fun getDealerCards(): List<Card> {
        return dealerHand.getCards()
    }

    private fun getPlayerCards(): List<Card> {
        return playerHand.getCards()
    }

    private fun setDealerCards(cards: List<Card>) {
        dealerHand.clear()
        dealerHand.addCards(cards)
        val dealerCardsString = cards.joinToString(",") { it.toString() }
        // Отправка полученных карт дилера всем клиентам
        for (controller in arrayClients) {
            controller.sendToHost("DealerCards:$dealerCardsString")
        }
    }

    private fun setPlayerCards(cards: List<Card>) {
        playerHand.clear()
        playerHand.addCards(cards)
        val playerCardsString = cards.joinToString(",") { it.toString() }
        // Отправка полученных карт игрока всем клиентам
        for (controller in arrayClients) {
            controller.sendToHost("PlayerCards:$playerCardsString")
        }
    }
}



