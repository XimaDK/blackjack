package com.example.blackjack


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.io.Serializable


class Deck(private val context: Context) {
    private val cards: MutableList<Card> = mutableListOf()

    init {
        // Создаем колоду карт
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                val card = Card(suit, rank)
                cards.add(card)
            }
        }
        shuffleDeck()
    }

    private fun shuffleDeck() {
        cards.shuffle()
    }

    fun dealCards(count: Int): List<Card> {
        val dealtCards = mutableListOf<Card>()
        repeat(count) {
            if (cards.isNotEmpty()) {
                val card = cards.removeAt(0)
                dealtCards.add(card)
            }
        }
        return dealtCards
    }

    fun displayCardImage(cardImageView: ImageView, card: Card) {
        val cardName = "${card.rank.name}_${card.suit.name}"
        val resourceId = context.resources.getIdentifier(cardName, "drawable", context.packageName)
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, resourceId)

        val targetWidth = bitmap.width / 2
        val targetHeight = bitmap.height / 2
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        cardImageView.setImageBitmap(resizedBitmap)
    }

    fun displayBackImage(cardImageView: ImageView){
        val resourceId = context.resources.getIdentifier("back", "drawable", context.packageName)
        val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val targetWidth = bitmap.width / 2
        val targetHeight = bitmap.height / 2
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        cardImageView.setImageBitmap(resizedBitmap)
    }

}


enum class Suit {
    heart, diamond, club, spade
}

enum class Rank {
    ace, two, three, four, five, six, seven, eight, nine, ten, jack, queen, king
}

class Card(val suit: Suit, val rank: Rank) : Serializable{
    override fun toString(): String {
        return "${rank.name}_${suit.name}"
    }
}


class Hand : Serializable {
    private val cards: MutableList<Card> = mutableListOf()

    fun addCards(newCards: List<Card>) {
        cards.addAll(newCards)
    }

    fun getCards(): List<Card> {
        return cards
    }

    fun clear() {
        cards.clear()
    }

    fun getHandValue(): Int {
        var handValue = 0
        var aceCount = 0

        for (card in cards) {
            val rank = card.rank
            when (rank) {
                Rank.ace -> {
                    handValue += 11
                    aceCount++
                }
                Rank.two, Rank.three, Rank.four, Rank.five, Rank.six, Rank.seven, Rank.eight, Rank.nine, Rank.ten -> {
                    handValue += rank.ordinal + 1
                }
                Rank.jack, Rank.queen, Rank.king -> {
                    handValue += 10
                }
            }
        }

        // Проверяем, если есть тузы и их сумма превышает 21, то учитываем тузы как 1
        while (handValue > 21 && aceCount > 0) {
            handValue -= 10
            aceCount--
        }

        return handValue
    }
}

object CardUtils {
    fun createCardFromString(cardString: String): Card {
        val parts = cardString.split("_")
        val rankString = parts[0]
        val suitString = parts[1]
        val rank = Rank.valueOf(rankString)
        val suit = Suit.valueOf(suitString)
        return Card(suit, rank)
    }
}