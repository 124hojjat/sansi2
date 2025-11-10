package sansino.sansino.controler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import sansino.sansino.model.reserve.Reservation

@Controller
class ReservationWebSocketController{


    @Autowired
    private lateinit var messagingTemplate: SimpMessagingTemplate


    fun notifyReservationCreated(reservation: Reservation) {
        messagingTemplate.convertAndSend("/topic/reservations", ReservationEvent("CREATED", reservation.id))
    }

    fun notifyReservationCancelled(reservation: Reservation) {
        messagingTemplate.convertAndSend("/topic/reservations", ReservationEvent("CANCELLED", reservation.id))
    }

    fun notifyReservationConfirmed(reservation: Reservation) {
        messagingTemplate.convertAndSend("/topic/reservations", ReservationEvent("CONFIRMED", reservation.id))
    }
}

data class ReservationEvent(
    val type: String,     // CREATED, CANCELLED, CONFIRMED
    val reservationId: Long
)
