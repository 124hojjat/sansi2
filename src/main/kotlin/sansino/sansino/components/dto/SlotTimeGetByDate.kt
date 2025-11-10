package sansino.sansino.components.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Version
import sansino.sansino.model.enums.SlotTimeStatus
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Reservation
import sansino.sansino.model.reserve.Salons
import java.time.LocalDate
import java.time.LocalTime

data class SlotTimeGetByDate(
    val id:Long,
    var status: SlotTimeStatus? = null,
    var capasity: Int = 0,
    var date: LocalDate = LocalDate.now(),       // روز سانس
    var startTime: LocalTime = LocalTime.of(0, 0), // زمان شروع
    var endTime: LocalTime = LocalTime.of(0, 0),   // زمان پایان
    var activity:List<ActivitiesSalonsAndUsers>

)