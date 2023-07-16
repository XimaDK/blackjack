package com.example.blackjack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class GameActivity : AppCompatActivity() {
    private lateinit var playerCardsContainer: LinearLayout
    private lateinit var opponentCardsContainer: LinearLayout
    private lateinit var deck: Deck
    private val mainHandler = Handler(Looper.getMainLooper())
    private var playerHand = Hand()
    private lateinit var playerContainers: Array<LinearLayout>
    private var playerCount = 0
    private lateinit var valueCards : TextView
    private lateinit var infoView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        playerCardsContainer = findViewById(R.id.playerCardsContainer)
        opponentCardsContainer = findViewById(R.id.opponentCardsContainer)
        valueCards = findViewById(R.id.valueCardsOfPlayer)
        deck = Deck(this)



        playerCount = intent.getIntExtra("playerCount", 0)
        playerHand.addCards(deck.dealCards(2))
        createPlayerContainers(playerCount)
        displayBackImageForPlayers()
        valueCards.text = "Суммарное количество очков: ${playerHand.getHandValue()}"

        val btnHit = findViewById<Button>(R.id.HitCard)
        btnHit.setOnClickListener{
            playerHand.addCards(deck.dealCards(1))
            updatePlayerHandView()
            valueCards.text = "Суммарное количество очков: ${playerHand.getHandValue()}"
        }

        val btnEnough = findViewById<Button>(R.id.enoughCards)
        btnEnough.setOnClickListener{
        }

        val textInfo = findViewById<TextView>(R.id.textInfo)

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

    private fun displayBackImageForPlayers() {
        playerCardsContainer.removeAllViews()
        opponentCardsContainer.removeAllViews()



        // Добавляем карты игрока в нижний контейнер
        for (card in playerHand.getCards()) {
            val cardImageView = ImageView(this)
            deck.displayCardImage(cardImageView, card)
            playerCardsContainer.addView(cardImageView)
        }

        // Отображаем рубашки карт других игроков в верхнем контейнере
        for (i in 0 until playerCount) {
            val opponentContainer = playerContainers[i]
            for (j in 0 until 2) {
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

}