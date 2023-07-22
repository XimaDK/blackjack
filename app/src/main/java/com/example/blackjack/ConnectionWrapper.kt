package com.example.blackjack

import java.lang.ref.WeakReference

object ConnectionWrapper {
    private var host: WeakReference<Host>? = null
    private var client: WeakReference<Client>? = null

    fun setHost(host: Host) {
        this.host = WeakReference(host)
    }

    fun getHost(): Host? {
        return host?.get()
    }

    fun getClient() : Client?{
        return client?.get()
    }

    fun setClient(client: Client){
        this.client = WeakReference(client)
    }

}