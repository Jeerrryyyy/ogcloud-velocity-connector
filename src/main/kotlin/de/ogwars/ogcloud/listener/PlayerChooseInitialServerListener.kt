package de.ogwars.ogcloud.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import de.ogwars.ogcloud.OgCloudConnectorPlugin
import net.kyori.adventure.text.Component
import org.kodein.di.instance

class PlayerChooseInitialServerListener {

    private val plugin: OgCloudConnectorPlugin by OgCloudConnectorPlugin.KODEIN.instance()

    @Subscribe
    fun onPlayerChooseInitialServer(event: PlayerChooseInitialServerEvent) {
        val player = event.player

        val server = plugin.proxyServer.allServers
            .filter { it.serverInfo.name.startsWith("server-lobby") }
            .minByOrNull { it.playersConnected.size }

        if (server == null) {
            player.disconnect(Component.text("no server for u i guess"))
            return
        }

        event.setInitialServer(server)
    }
}