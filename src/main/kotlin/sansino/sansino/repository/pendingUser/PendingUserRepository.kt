package sansino.sansino.repository.pendingUser

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.pendingUser.PendingUser

@Repository
interface PendingUserRepository : JpaRepository<PendingUser, Long> {
    fun findByPhoneNumberAndOtpCode(numberPhone: String, otpCode: String): PendingUser?
}