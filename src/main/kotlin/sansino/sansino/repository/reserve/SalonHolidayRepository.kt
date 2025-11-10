package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import sansino.sansino.model.reserve.SalonHoliday
import sansino.sansino.model.reserve.Salons
import java.time.LocalDate
import java.util.*

interface SalonHolidayRepository :JpaRepository<SalonHoliday,Long>{

    fun findBySalonAndDateAfter(salons: Salons,date :LocalDate):List<SalonHoliday>

    fun existsBySalonAndDate(salons: Salons, date :LocalDate):Boolean

    fun findBySalonAndDate(salons: Salons, date :LocalDate):SalonHoliday
    fun findBySalon(salons: Salons):List<SalonHoliday>
}