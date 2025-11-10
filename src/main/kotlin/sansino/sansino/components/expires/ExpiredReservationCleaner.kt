package sansino.sansino.components.expires

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import sansino.sansino.servise.reserve.ReservationService

@Service
class ExpiredReservationCleaner {
    @Autowired
    private lateinit var reservationService: ReservationService

    @Scheduled(fixedRate = 60_000) // هر ۱ دقیقه چک کن
    @Transactional
    fun cleanExpiredReservations() {
        reservationService.cancelExpiredReservations()

    }
}