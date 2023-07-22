package com.example.blackjack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class GameActivity : AppCompatActivity(), GameListener {
    private lateinit var playerCardsContainer: LinearLayout
    private lateinit var opponentCardsContainer: LinearLayout
    private lateinit var deck: Deck
    private var playerHand = Hand()
    private lateinit var playerContainers: Array<LinearLayout>
    private var playerCount = 0
    private lateinit var valueCards: TextView
    private lateinit var infoView: TextView
    private lateinit var btnHit: Button
    private lateinit var btnEnough: Button
    private  var host: Host? = null
    private var client : Client? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        infoView = findViewById(R.id.textInfo)

        playerCardsContainer = findViewById(R.id.playerCardsContainer)
        opponentCardsContainer = findViewById(R.id.opponentCardsContainer)
        valueCards = findViewById(R.id.valueCardsOfPlayer)
        deck = Deck(this)

        playerCount = intent.getIntExtra("playerCount", 0)
        playerHand.addCards(deck.dealCards(2))
        createPlayerContainers(playerCount)
        displayBackImageForPlayers()
        valueCards.text = "Суммарное количество очков: ${playerHand.getHandValue()}"

        btnHit = findViewById(R.id.HitCard)
        btnHit.setOnClickListener {
            playerHand.addCards(deck.dealCards(1))
            updatePlayerHandView()
            valueCards.text = "Суммарное количество очков: ${playerHand.getHandValue()}"

            if (host!= null) {
                host?.updateDealerCardCount(1)
                host?.sendMessageToClients("Дилер ${host?.getUsername()} взял еще одну карту!")
            }
            if (client != null){
                client?.updatePlayerCardCount(1)
                client?.sendToHost("InfoMessage: Игрок ${client?.getNickname()} взял еще одну карту!",
                    client?.getNickname().toString())
            }
        }

        btnEnough = findViewById(R.id.enoughCards)
        btnEnough.setOnClickListener {
            if (host!= null) {
                host?.passTurnToNextPlayer()
                host?.sendMessageToClients("Дилер ${host?.getUsername()} закончил игру со счетом ${playerHand.getHandValue()}. Теперь ходит следующий игрок")

            }
            if (client != null){
                client?.passTurnToNextPlayer()
                client?.sendToHost("InfoMessage: Игрок ${client?.getNickname()} закончил игру со счетом " +
                        "${playerHand.getHandValue()}. Теперь ходит следующий игрок", client?.getNickname().toString())
            }
        }

        client = ConnectionWrapper.getClient()
        host = ConnectionWrapper.getHost()
        client?.setTurnListener(this)
        host?.setTurnListener(this)

        if (host != null){
            onPlayerTurn()
        }
        else onOpponentTurn()
    }

    private fun updatePlayerHandView() {
        playerCardsContainer.removeAllViews()

        for (card in playerHand.getCards()) {
            val cardImageView = ImageView(this)
            deck.displayCardImage(cardImageView, card)
            playerCardsContainer.addView(cardImageView)
        }

    }

    private fun createPlayerContainers(playerCount: Int) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(
            0,
            0,
            600,
            0
        )

        playerContainers = Array(playerCount) { LinearLayout(this) }

        for (i in 0 until playerCount) {
            val opponentContainer = LinearLayout(this)
            opponentContainer.orientation = LinearLayout.HORIZONTAL
            opponentContainer.layoutParams = layoutParams
            playerContainers[i] = opponentContainer
            opponentCardsContainer.addView(opponentContainer)
        }
    }

    private fun displayBackImageForPlayers(cardCount: Int = 2) {
        playerCardsContainer.removeAllViews()
        opponentCardsContainer.removeAllViews()


        for (card in playerHand.getCards()) {
            val cardImageView = ImageView(this)
            deck.displayCardImage(cardImageView, card)
            playerCardsContainer.addView(cardImageView)
        }

        for (i in 0 until playerCount) {
            val opponentContainer = playerContainers[i]
            for (j in 0 until cardCount) {
                val cardImageView = ImageView(this)
                deck.displayBackImage(cardImageView)
                cardImageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                opponentContainer.addView(cardImageView)
            }
            opponentCardsContainer.addView(opponentContainer)
        }
    }

    override fun onPlayerTurn() {
        runOnUiThread {
            btnHit.isEnabled = true
            btnEnough.isEnabled = true
        }
    }

    override fun onOpponentTurn() {
        runOnUiThread {
            btnHit.isEnabled = false
            btnEnough.isEnabled = false
        }
    }
    override fun playerCardCount(cardCount: Int) {
        runOnUiThread {
            displayBackImageForPlayers(cardCount)
        }
    }

    override fun getHand() : Hand {
        return playerHand
    }

    override fun revealPlayerCards(playerCards: List<Card>, playerIndex: Int) {
        runOnUiThread {
            if (playerIndex == 0) {
                val opponentContainer = playerContainers[0]
                opponentContainer.removeAllViews()
                for (card in playerCards) {
                    val cardImageView = ImageView(this)
                    deck.displayCardImage(cardImageView, card)
                    opponentContainer.addView(cardImageView)
                }
            } else {
                val opponentContainer = playerContainers[playerIndex - 1]
                opponentContainer.removeAllViews()
                for (card in playerCards) {
                    val cardImageView = ImageView(this)
                    deck.displayCardImage(cardImageView, card)
                    opponentContainer.addView(cardImageView)
                }
            }
        }
    }

    override fun setTextInfoView(message: String) {
        runOnUiThread {
        infoView.text = message
        }
    }
}