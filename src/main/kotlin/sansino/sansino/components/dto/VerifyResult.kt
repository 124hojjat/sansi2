package sansino.sansino.components.dto

data class VerifyResult(
    val success: Boolean,
    val message: String,
    val orderId: Long,
    val refId: String? = null
)
