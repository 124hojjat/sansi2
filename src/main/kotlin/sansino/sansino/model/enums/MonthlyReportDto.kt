package sansino.sansino.model.enums

import java.math.BigDecimal

data class MonthlyReportDto(
    val year: Int,
    val month: Int,
    val totalAmount: BigDecimal
)
