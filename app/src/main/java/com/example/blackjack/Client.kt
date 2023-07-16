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

    fun sayReady(){
        sendToHost("PlayerReady", nickname)
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
    }

}
