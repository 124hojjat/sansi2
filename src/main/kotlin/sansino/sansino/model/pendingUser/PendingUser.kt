package sansino.sansino.model.pendingUser

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.genderStatus
import java.time.LocalDateTime

@Entity
data class PendingUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val gender: genderStatus = genderStatus.MAN,
    val password: String? = null,
    val phoneNumber: String = "",
    val role: Role =Role.KARBAR,
    val otpCode: String = "",
    val expiresAt: LocalDateTime? = null
)

