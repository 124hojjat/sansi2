package sansino.sansino.components.dto

import sansino.sansino.model.enums.genderStatus

data class RegisterRequest(
    val phone: String,
    val password: String?,
    val name: String,
    val genderStatus: genderStatus
)
