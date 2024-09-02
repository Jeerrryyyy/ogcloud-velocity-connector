package de.ogwars.ogcloud

import com.google.gson.GsonBuilder
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import de.ogwars.ogcloud.kafka.KafkaConnection
import de.ogwars.ogcloud.listener.PlayerChooseInitialServerListener
import de.ogwars.ogcloud.listener.ProxyPingListener
import de.ogwars.ogcloud.message.ServerReadyMessage
import de.ogwars.ogcloud.message.ServerRemoveMessage
import de.ogwars.ogcloud.server.ServerHelper
import de.ogwars.ogcloud.store.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@Plugin(
    id = "ogcloud-velocity-connector",
    name = "OgCloud Connector Plugin",
    version = "1.0.0",
    authors = ["Jevzo"],
)
class OgCloudConnectorPlugin @Inject constructor(
    val proxyServer: ProxyServer,
    private val logger: Logger,

    @Suppress("unused")
    @DataDirectory
    val dataDirectory: Path,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    companion object {
        lateinit var KODEIN: DI
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        val serverName = System.getenv("OGCLOUD_SERVER_NAME")
        val clusterAddress = System.getenv("OGCLOUD_CLUSTER_ADDRESS")

        logger.info("ServerName: $serverName | ClusterAddress: $clusterAddress")

        onEnable()
    }

    private fun onEnable() {
        initializeDI()

        proxyServer.eventManager.register(this, ProxyPingListener())
        proxyServer.eventManager.register(this, PlayerChooseInitialServerListener())

        proxyServer.scheduler.buildTask(this, Runnable {
            listenServersReadyTopic()
            listenServersRemoveTopic()
        }).delay(1, TimeUnit.SECONDS).schedule()

        logger.info("OgCloud Velocity Connector init complete...")
    }

    private fun initializeDI() {
        KODEIN = DI {
            bindSingleton { this@OgCloudConnectorPlugin }
            bindSingleton { Store() }
            bindSingleton { GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create() }
            bindSingleton { ServerHelper(instance(), instance()) }
            bindSingleton {
                KafkaConnection(
                    // TODO: Fetch through config
                    "ogcloud-kafka-0.ogcloud-kafka.default.svc.cluster.local:9092",
                    "ogcloud-velocity-consumer",
                    instance(),
                )
            }
        }
    }

    private fun listenServersReadyTopic() =
        KODEIN.direct.instance<KafkaConnection>().listenOnTopic<ServerReadyMessage>("servers-ready") {
            KODEIN.direct.instance<ServerHelper>().addServer(it)
        }

    private fun listenServersRemoveTopic() =
        KODEIN.direct.instance<KafkaConnection>().listenOnTopic<ServerRemoveMessage>("servers-remove") {
            KODEIN.direct.instance<ServerHelper>().removeServer(it)
        }
}