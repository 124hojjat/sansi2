package sansino.sansino.servise.fcm

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.time.Duration

@Service
class FcmService {

    fun sendNotification(token: String, title: String, body: String) {

        val notif =Notification.builder()
            .setBody(body)
            .setTitle(title)
            .build()
        val ttlMillis = Duration.ofHours(1).toMillis()
        val message = Message.builder()
            .setToken(token)
            .setNotification(notif)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setTtl(ttlMillis) // نگهداری تا ۱ ساعت اگه کاربر آفلاین بود
                    .build()
            )
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        println("✅ Message sent: $response")
    }

    fun sendData(token: String, data: Map<String, String>) {
        val ttlMillis = Duration.ofHours(1).toMillis()
        val message = Message.builder()
            .setToken(token)
            .putAllData(data)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setTtl(ttlMillis)
                    .build()
            )
            .build()

        val response = FirebaseMessaging.getInstance().send(message)
        println("✅ Data message sent: $response")
    }
}
