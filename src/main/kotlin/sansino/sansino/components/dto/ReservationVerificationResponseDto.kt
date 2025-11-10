package sansino.sansino.components.dto

import sansino.sansino.model.reserve.SlotTime
import java.time.LocalTime

data class ReservationVerificationResponseDto(
    val success: Boolean,
    val userName: String,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val message: String
)
