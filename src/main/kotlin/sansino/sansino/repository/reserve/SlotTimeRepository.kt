package sansino.sansino.repository.reserve

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.reserve.Salons
import sansino.sansino.model.reserve.SlotTime
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@Repository
interface SlotTimeRepository: JpaRepository<SlotTime, Long> {

     fun findBySalonsAndDate(salons: Salons, date: LocalDate): List<SlotTime>
     fun findBySalonsIdAndDate(
          salonsId: Long,
          date: LocalDate,
          pageable: PageRequest
     ): Page<SlotTime>

     fun existsBySalonsAndDate(salons: Salons, date: LocalDate): Boolean

     fun existsBySalonsAndDateBetween(salon: Salons, startDate: LocalDate, endDate: LocalDate): Boolean

     fun findAllBySalonsId(salonsId: Long,pageRequest: PageRequest): List<SlotTime>

     fun findBySalonsAndDateAndStartTime(salons: Salons, date: LocalDate, startTime: LocalTime): SlotTime?
     fun findBySalonsAndDateAndEndTime(salons: Salons, date: LocalDate, endTime: LocalTime): SlotTime?


}