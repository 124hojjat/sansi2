package sansino.sansino.components

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import sansino.sansino.model.enums.Role
import sansino.sansino.model.reserve.User
import sansino.sansino.repository.reserve.UserRepository

@Service
class BootstrapService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Value("\${bootstrap.admin.phone:}")
    private lateinit var adminPhone: String

    @Value("\${bootstrap.admin.name:}")
    private lateinit var adminName: String

    @Value("\${bootstrap.admin.password:}")
    private lateinit var adminPassword: String

    @PostConstruct
    fun initAdmin() {
        try {
            // اگر مشخصات ست نشده‌اند، کاری نکن
            if (adminPhone.isBlank() || adminPassword.isBlank()) {
                println("Bootstrap admin variables not provided — skipping admin creation.")
                return
            }

            // فقط اگر هیچ ادمینی وجود نداشته باشد
            if (userRepository.countByRole(Role.ADMIN) == 0L) {
                if (userRepository.existsByNumberPhone(adminPhone)) {
                    // اگر یوزر با همان شماره وجود دارد، به‌جای تضعیف امنیت بهتر لاگ کن و برگرد
                    println("User with bootstrap phone already exists — skipping admin creation.")
                    return
                }

                val admin = User(
                    numberPhone = adminPhone,
                    name = adminName.takeIf { it.isNotBlank() } ?: adminName,
                    password = passwordEncoder.encode(adminPassword),
                    role = Role.ADMIN
                )
                userRepository.save(admin)
                println("✅ Bootstrap admin created for phone=$adminPhone")
            } else {
                println("Admin user(s) already exist — no bootstrap needed.")
            }
        } catch (ex: Exception) {
            // هرگز پسورد را لاگ نکن
            println("Error during bootstrap admin creation: ${ex.message}")
        }
    }
}
