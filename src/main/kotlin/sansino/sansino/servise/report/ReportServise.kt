package sansino.sansino.servise.report

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.Role
import sansino.sansino.model.reports.Reports
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import java.time.LocalDate
import java.time.LocalDateTime


@Service
class ReportServise {


    @Autowired
    private lateinit var salonsRepository: SalonsRepository

    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository

    //    کل گزارش های تمام سالن ها
    fun getAllReports(token: String,pageIndex: Int, pageSize: Int): List<Reports> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("دسترسی برای شما فعال نیست")

        val pagRequest = PageRequest.of(pageIndex, pageSize, Sort.by("date"))
        val data = reportsRepository.findAll(pagRequest)
        return data.content
    }

    //    کل گزارش های یک سالن
    fun getByHallId(token: String, hallId: Long, pageIndex: Int, pageSize: Int): List<Reports> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN && user.role != Role.HOLDER) throw ExceptionMe("شما دسترسی به این اطلاعات ندارید")
        val pagRequest = PageRequest.of(pageIndex, pageSize, Sort.by("date"))
        if (user.role == Role.ADMIN) {
            val data = reportsRepository.findByHallId(hallId, pagRequest)
            return data
        } else {
            val salon = salonsRepository.findSalonsByOwnerId(userId)
            val data = reportsRepository.findByHallId(salon.first().id, pagRequest)
            return data
        }
    }

    //   ✔ کل گزارش های یک شخص
    fun getByUserId(token: String, userIdKarbar: Long, pageIndex: Int, pageSize: Int): List<Reports> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("دسترسی برای شما فعال نیست")
        val pagRequest = PageRequest.of(pageIndex, pageSize, Sort.by("id"))
        val data = reportsRepository.findByUserId(userIdKarbar, pagRequest)
        return data
    }

    //    ✔کل گزارش های بین دو تاریخ
    fun findCreatedAtBetween(
        token: String,
        start: LocalDateTime,
        end: LocalDateTime,
        pageIndex: Int,
        pageSize: Int
    ): List<Reports> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (start.isAfter(end)) throw ExceptionMe("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        if (user.role != Role.ADMIN) throw ExceptionMe("دسترسی برای شما فعال نیست")
        val pagRequest = PageRequest.of(pageIndex, pageSize, Sort.by("id"))
        val data = reportsRepository.findByCreatedAtBetween(start, end, pagRequest)
        return data
    }

    //     کل گزارش های یک سالن بین دو تاریخ با نوع وضعیت پرداختی✔
    fun findAllByHallIdAndDateRangeAndPaymentMethod(
        token: String,
        hallId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
        paymentMethod: PaymentMethod,
        reservationStatue: ReservationStatue,
        pageIndex: Int,
        pageSize: Int
    ): List<Reports> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (startDate.isAfter(endDate)) throw ExceptionMe("تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد")
        if (user.role != Role.ADMIN && user.role != Role.HOLDER) throw ExceptionMe("شما دسترسی به این اطلاعات ندارید")
        val salon = salonsRepository.findById(hallId).orElseThrow { ExceptionMe("سالن پیدا نشد") }
        if (user.role == Role.HOLDER && salon.owner?.id != user.id) {
            throw ExceptionMe("شما دسترسی به سالن های دیگر ندارید")
        }

        val pageRequest = PageRequest.of(
            pageIndex,
            pageSize,
            Sort.by(Sort.Direction.DESC, "id")
        )
        return reportsRepository.findAllByHallIdAndDateBetweenAndPaymentMethodAndReservationStatus(
            hallId = hallId,
            startDate = startDate,
            endDate = endDate.plusDays(1),
            paymentMethod = paymentMethod,
            reservationStatue = reservationStatue,
            pageRequest = pageRequest
        )
    }


    fun findByReservationId(reservationId: Long): Reports {
        val data = reportsRepository.findByReservationId(reservationId)
        return data
    }

}