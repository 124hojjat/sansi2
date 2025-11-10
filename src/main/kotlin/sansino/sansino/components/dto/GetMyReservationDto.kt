package sansino.sansino.components.dto

import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.genderStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class GetMyReservationDto(
    var id :Long,
    var status: ReservationStatue = ReservationStatue.PENDING,
    var amount: String? = null,
    var date: LocalDate = LocalDate.now(),       // روز سانس
    var startTime: LocalTime = LocalTime.of(0, 0), // زمان شروع
    var endTime: LocalTime = LocalTime.of(0, 0),   // زمان پایان
    var nameSalon :String? = null,
    var numberPhoneKarbar:String,
    var ticketCode:String?
    )
