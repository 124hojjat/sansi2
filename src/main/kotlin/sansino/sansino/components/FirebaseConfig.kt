package sansino.sansino.components

/*import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccountPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
                ?: throw RuntimeException("Firebase credentials not found in env variable")

            val serviceAccount = java.io.FileInputStream(serviceAccountPath)
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
        }
    }
}*/
