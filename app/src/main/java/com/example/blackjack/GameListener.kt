package com.example.blackjack

import android.os.Message

interface GameListener {
    fun onPlayerTurn()
    fun onOpponentTurn()
    fun playerCardCount(cardCount: Int)
    fun getHand() : Hand
    fun revealPlayerCards(playerCards: List<Card>, playerIndex: Int)
    fun setTextInfoView(message: String)
}

