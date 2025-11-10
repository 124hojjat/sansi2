package sansino.sansino.components.dto

import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Salons

data class SalonInsertRequest(
    val salons: Salons,
    val activitiesSalonsAndUsers: MutableList<ActivitiesSalonsAndUsers>,
    val numberPhoneOwner: String
)