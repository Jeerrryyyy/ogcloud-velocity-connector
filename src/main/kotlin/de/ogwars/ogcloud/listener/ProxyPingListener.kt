package de.ogwars.ogcloud.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyPingEvent
import net.kyori.adventure.text.Component

class ProxyPingListener {

    @Subscribe
    fun onProxyPingEvent(event: ProxyPingEvent) {
        // TODO: Do overwrite motd
        val responseBuilder = event.ping.asBuilder()
        responseBuilder.description(Component.text("Hello, World\nCooooolll"))
        event.ping = responseBuilder.build()
    }
}