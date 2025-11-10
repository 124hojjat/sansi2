package sansino.sansino.repository.report

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.reports.Reports
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface ReportsRepository : JpaRepository<Reports, Long> {
    // گرفتن گزارش‌های یک سالن خاص
    fun findByHallId(hallId: Long,pageable: PageRequest): List<Reports>

    // گرفتن گزارش‌های یک کاربر خاص
    fun findByUserId(userId: Long, pagRequest: PageRequest): List<Reports>

    // گزارش‌ها بین دو تاریخ خاص
    fun findByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime,pageable: PageRequest): List<Reports>

    // گزارش‌ها بر اساس وضعیت رزرو
    fun findByReservationStatus(status: ReservationStatue): List<Reports>

    // 📌 گزارش بر اساس زمان شروع و پایان و ایدی سالن و روش پرداخت
    fun findAllByHallIdAndDateBetweenAndPaymentMethodAndReservationStatus(
        hallId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        paymentMethod: PaymentMethod,
        reservationStatue: ReservationStatue,
        pageRequest: Pageable,

    ): List<Reports>


    fun findByReservationId(reservationId: Long): Reports

    fun findByTimeSlotIdAndNumberPhone(slotTime:Long, numberPhone: String): List<Reports>



}