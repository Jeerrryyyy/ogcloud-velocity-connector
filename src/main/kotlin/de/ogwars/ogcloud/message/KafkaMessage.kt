package de.ogwars.ogcloud.message

interface KafkaMessage

data class ServerReadyMessage(
    val name: String,
    val clusterAddress: String
) : KafkaMessage

data class ServerRemoveMessage(
    val name: String
) : KafkaMessage