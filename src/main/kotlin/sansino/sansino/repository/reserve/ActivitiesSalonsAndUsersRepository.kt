package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers

@Repository
interface ActivitiesSalonsAndUsersRepository:JpaRepository<ActivitiesSalonsAndUsers,Long> {
}