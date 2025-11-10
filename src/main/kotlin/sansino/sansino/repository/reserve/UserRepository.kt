package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.enums.Role
import sansino.sansino.model.reserve.User


@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun countByRole(role: Role): Long
    fun existsByNumberPhone(number: String): Boolean
    fun findByNumberPhone(numberPhone: String): User?
}