package sansino.sansino.repository.tablighat

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.tablighat.Tablighat

@Repository
interface TablighatRepository : JpaRepository<Tablighat, Long> {
}