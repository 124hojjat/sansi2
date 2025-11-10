package sansino.sansino.components.dto

import jakarta.persistence.*
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.SlotTime
import sansino.sansino.model.reserve.User
import kotlin.system.exitProcess

data class SalonDto(
    var id: Long = 0,
    var name: String = "",
    var numberPhone: String = "",
    var address: String = "",
    var filmurl: String = "",
    val galleryImages: List<String>?, // گالری
    var betweenWomanMan:Boolean? = false,
    var slotTimes: MutableList<SlotTime>? = mutableListOf(),
    var activitis: MutableList<ActivitiesSalonsAndUsers> = mutableListOf(),
    var owner:String? = null
)
