package de.ogwars.ogcloud.server

import com.velocitypowered.api.proxy.server.ServerInfo
import de.ogwars.ogcloud.OgCloudConnectorPlugin
import de.ogwars.ogcloud.message.ServerReadyMessage
import de.ogwars.ogcloud.message.ServerRemoveMessage
import de.ogwars.ogcloud.store.Store
import net.kyori.adventure.text.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class ServerHelper(
    private val ogCloudConnectorPlugin: OgCloudConnectorPlugin,
    private val store: Store
) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun addServer(serverReadyMessage: ServerReadyMessage) {
        logger.info("Registering Server (ServerName: ${serverReadyMessage.name}, ClusterAddress: ${serverReadyMessage.clusterAddress})")
        val serverInfo = ServerInfo(
            serverReadyMessage.name,
            InetSocketAddress(serverReadyMessage.clusterAddress, 25565)
        )
        ogCloudConnectorPlugin.proxyServer.registerServer(serverInfo)
        notifyMembers(serverReadyMessage.name, NotificationType.ADDED)
    }

    fun removeServer(serverRemoveMessage: ServerRemoveMessage) =
        ogCloudConnectorPlugin.proxyServer.getServer(serverRemoveMessage.name).ifPresent {
            logger.info("Unregistering Server (ServerName: ${serverRemoveMessage.name})")

            ogCloudConnectorPlugin.proxyServer.unregisterServer(it.serverInfo)
            notifyMembers(serverRemoveMessage.name, NotificationType.REMOVED)
        }

    private fun notifyMembers(serverName: String, notificationType: NotificationType) {
        // TODO: Add permission back (.filter { it.hasPermission("ogcloud.notify.server") })
        ogCloudConnectorPlugin.proxyServer.allPlayers
            .forEach {
                when (notificationType) {
                    NotificationType.ADDED -> it.sendMessage(Component.text("${store.prefix} &7Server &a$serverName &7wurde &aregistriert!"))
                    NotificationType.REMOVED -> it.sendMessage(Component.text("${store.prefix} &7Server &c$serverName &7wurde &centfernt!"))
                }
            }
    }

    private enum class NotificationType {
        ADDED, REMOVED
    }
}