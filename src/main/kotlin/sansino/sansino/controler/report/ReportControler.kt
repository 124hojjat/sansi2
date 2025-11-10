package sansino.sansino.controler.report

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import sansino.sansino.components.ServiceResponse
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.reports.Reports
import sansino.sansino.servise.report.ReportServise
import java.time.LocalDate
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/report")
class ReportControler {

    @Autowired
    private lateinit var service: ReportServise


    @GetMapping("/getAll")
    fun getAll(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
    ): ServiceResponse<Reports> {
        return try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.getAllReports(token, pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    @GetMapping("/getByHallId")
    fun getByHallId(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam salonId: Long,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
    ): ServiceResponse<Reports> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.getByHallId(token, salonId, pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/getByUserId")
    fun getByUserId(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam userIdKarbar: Long,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
    ): ServiceResponse<Reports> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.getByUserId(token, userIdKarbar, pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/CreatedAtBetween")
    fun findCreatedAtBetween(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam start: LocalDateTime,
        @RequestParam end: LocalDateTime,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
    ): ServiceResponse<Reports> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.findCreatedAtBetween(token, start, end, pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/ByHallIdAndDateRangeAndPaymentMethod")
    fun findAllByHallIdAndDateRangeAndPaymentMethod(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam hallId: Long,
        @RequestParam start: LocalDate,
        @RequestParam end: LocalDate,
        @RequestParam paymentMethod: PaymentMethod,
        @RequestParam reservationStatue: ReservationStatue,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
    ): ServiceResponse<Reports> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.findAllByHallIdAndDateRangeAndPaymentMethod(
                token,
                hallId,
                start,
                end,
                paymentMethod,
                reservationStatue = reservationStatue,
                pageIndex,
                pageSize
            )
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


}