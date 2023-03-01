package com.example.apiempleado.config.websocket

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.SubProtocolCapable
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.time.LocalTime
import java.util.concurrent.CopyOnWriteArraySet

private val logger = KotlinLogging.logger {}

class WebSocketHandler(private val entity: String) : TextWebSocketHandler(), SubProtocolCapable, WebSocketSender {
    private val sessions: MutableSet<WebSocketSession> = CopyOnWriteArraySet()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info { "Conexión establecida con el servidor" }
        sessions.add(session)
        val message = TextMessage("Updates Web socket: $entity")
        session.sendMessage(message)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session)
    }

    override fun getSubProtocols(): List<String> {
        return listOf("subprotocol.demo.websocket")
    }

    override fun sendMessage(message: String) {
        logger.info { "Enviar mensaje de cambios en $entity: $message" }
        sessions.forEach { session ->
            if (session.isOpen) {
                logger.info { "Servidor envía: $message" }
                session.sendMessage(TextMessage(message))
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    @Throws(IOException::class)
    override fun sendPeriodicMessages() {
        for (session in sessions) {
            if (session.isOpen) {
                val broadcast = "server periodic message " + LocalTime.now()
                session.sendMessage(TextMessage(broadcast))
            }
        }
    }
}