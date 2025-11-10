package sansino.sansino.components.dto

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
)