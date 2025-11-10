package sansino.sansino.components.dto

import java.time.LocalDate

data class SalonHolyDayDto(
    val id:Long? = null,
    var date: LocalDate = LocalDate.now(),
    val salonName:String
    )