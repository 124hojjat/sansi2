package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import sansino.sansino.model.reserve.Canceling
import sansino.sansino.model.reserve.Salons


@Repository
interface CancelingRepository : JpaRepository<Canceling, Long> {

    @Query("""
    select m
    from Canceling m
    join m.reservation u
    join u.slotTime x 
    join x.salons y 
    where y.id = :salon
""")
    fun findAllByReservationSalon(@Param("salon") salon: Long): List<Canceling>


    @Query("""
        select m
    from Canceling m
    join m.reservation u
    join u.user x 
    where x.id = :userId
    """)
    fun findAllByReservationUser(@Param("userId") userId: Long): List<Canceling>

}