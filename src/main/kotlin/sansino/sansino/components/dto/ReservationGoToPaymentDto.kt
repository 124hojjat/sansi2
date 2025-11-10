package sansino.sansino.components.dto

import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.reserve.SlotTime
import java.time.LocalDate
import java.time.LocalTime

data class ReservationGoToPaymentDto(
    val reservationId:Long,
    val salonName:String,
    val date : LocalDate,
    val startTime: LocalTime,
    val endTime:LocalTime,
    val amount:String,
    val gender:genderStatus,
    val name:String,
    )
