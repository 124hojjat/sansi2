package sansino.sansino.components

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")   // همه کلاینت‌ها اینجا subscribe می‌کنن
        config.setApplicationDestinationPrefixes("/app") // پیام‌هایی که از کلاینت میان
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")           // آدرس WebSocket
            .setAllowedOriginPatterns("*")    // اجازه به همه origin ها
            .withSockJS()                     // fallback برای مرورگرهای قدیمی
    }
}