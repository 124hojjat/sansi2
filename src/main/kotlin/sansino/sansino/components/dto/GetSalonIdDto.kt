package sansino.sansino.components.dto

import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.ImagesSalon
import sansino.sansino.model.reserve.SlotTime

data class GetSalonIdDto(
    val id:Long,
    val name:String,
    var address:String,
    var filmUrl:String,
    var betweenWomanMan: Boolean,
    var imageurls: MutableList<ImagesSalon>,
    var activitis: MutableList<ActivitiesSalonsAndUsers>,
    )
