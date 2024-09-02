package de.ogwars.ogcloud.kafka

import com.google.gson.Gson
import de.ogwars.ogcloud.message.KafkaMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

class KafkaConnection(
    bootstrapServers: String,
    groupId: String,
    val gson: Gson
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    var consumer: KafkaConsumer<String, String>

    init {
        val properties = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
        }

        consumer = KafkaConsumer<String, String>(properties)
    }

    inline fun <reified T : KafkaMessage> listenOnTopic(
        topic: String,
        crossinline messageHandler: (T) -> Unit
    ) {
        try {
            consumer.subscribe(listOf(topic))

            while (true) {
                val records = consumer.poll(Duration.ofMillis(TimeUnit.SECONDS.toMillis(5)))

                records.forEach { record ->
                    try {
                        val recordValue = record.value()
                        messageHandler(gson.deserializeKafkaValue<T>(recordValue))
                    } catch (e: Exception) {
                        logger.error("Error processing message: ${e.message}")
                    }
                }
            }
        } finally {
            consumer.close()
        }
    }
}

inline fun <reified T : KafkaMessage> Gson.deserializeKafkaValue(value: String): T = fromJson(value, T::class.java)