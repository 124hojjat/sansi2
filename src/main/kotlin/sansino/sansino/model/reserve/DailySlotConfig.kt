package sansino.sansino.model.reserve

import jakarta.persistence.Embeddable

@Embeddable
data class DailySlotConfig(
    var startHour: Int = 8,
    var startMinute: Int= 0,
    var endHour: Int = 16,
    var endMinute: Int= 0,
    var durationMinutes: Int= 120,
    var capacity: Int = 5
)