package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.reserve.Reservation
import sansino.sansino.model.reserve.SlotTime
import sansino.sansino.model.reserve.User
import java.util.Date


@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {

    fun findByStatusAndExpiresAtBefore(status: ReservationStatue,expiresAt :Long):List<Reservation>

    fun findAllByUser(user: User): List<Reservation>

    fun findByTicketCode(ticketCode: String): Reservation?

    fun findByStatus(status: ReservationStatue): List<Reservation>
    fun findAllByUserAndSlotTime(user: User, slotTime: SlotTime): List<Reservation>


    fun existsByTicketCode(ticketCode: String): Boolean

}
