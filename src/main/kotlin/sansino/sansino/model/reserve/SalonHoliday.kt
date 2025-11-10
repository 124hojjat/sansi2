package sansino.sansino.model.reserve

import jakarta.persistence.*
import java.time.LocalDate

@Entity
data class SalonHoliday(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    var date: LocalDate = LocalDate.now(),

    @ManyToOne
    var salon: Salons? = null
)