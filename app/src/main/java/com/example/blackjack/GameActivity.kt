package com.example.blackjack

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout


class GameActivity : AppCompatActivity() {
    private lateinit var dealerCardsContainer: LinearLayout
    private lateinit var playerCardsContainer: LinearLayout
    private lateinit var deck: Deck
    private lateinit var dealerHand: Hand
    private lateinit var playerHand: Hand

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        dealerCardsContainer = findViewById(R.id.dealerCardsContainer)
        playerCardsContainer = findViewById(R.id.playerCardsContainer)

        deck = Deck(this)

        dealerHand = (intent.getSerializableExtra("dealerHand") as? Hand)!!
        playerHand = (intent.getSerializableExtra("playerHand") as? Hand)!!


        updateDealerCards(dealerHand.getCards())
        updatePlayerCards(playerHand.getCards())

    }

    fun updateDealerCards(cards: List<Card>) {
        mainHandler.post {
            dealerCardsContainer.removeAllViews()
            for (card in cards) {
                val cardImageView = ImageView(this)
                deck.displayCardImage(cardImageView, card)
                dealerCardsContainer.addView(cardImageView)
            }
        }
    }

    fun updatePlayerCards(cards: List<Card>) {
        mainHandler.post {
            playerCardsContainer.removeAllViews()
            for (card in cards) {
                val cardImageView = ImageView(this)
                deck.displayCardImage(cardImageView, card)
                playerCardsContainer.addView(cardImageView)
            }
        }
    }
}