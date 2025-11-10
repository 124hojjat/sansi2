package sansino.sansino.controler.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ServiceResponse
import sansino.sansino.model.reserve.Canceling
import sansino.sansino.servise.reserve.CancelingService


@RestController
@RequestMapping("/api/canceling")
class CancelingController {

    @Autowired
    private lateinit var service: CancelingService

    //    کاربر شروع میکنه به کنسل کردن
    @PostMapping("/addCanceling")
    fun addCanceling(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam reservationId: Long,
        @RequestParam refundMethod: String
    ): ServiceResponse<Canceling> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.addCanceling(token,reservationId, refundMethod)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(
                data = null,
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "${e.message}"
            )
        }
    }


    //    ادمین میخواد کنسلی ها رو پرداخت کنه
    @PutMapping("/adminCanceling")
    fun adminCanceling(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam reservationId: Long,
        @RequestParam cancelingId: Long,
        @RequestParam trackingCode: String
    ): ServiceResponse<Canceling> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.adminCanceling(
                token,
                reservationId = reservationId,
                cancelingId = cancelingId,
                trackingCode = trackingCode
            )
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(
                data = null,
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "${e.message}"
            )
        }
    }


    //    ادمین میخواد تمام کنسلی ها رو ببینه
    @GetMapping("/getAll")
    fun getAll(
        @RequestHeader("Authorization") authHeader: String?
    ): ServiceResponse<Canceling> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.getAll(token)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //    ادمین یا سالن دار میخوان بر اساس سالن کنسلی ها رو پیدا کنن
    @GetMapping("/getBySalons")
    fun getBySalons(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam salonsId: Long
    ): ServiceResponse<Canceling> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.getBySalons(token,salonsId = salonsId)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //    یوزر یا ادمین میخوان کنسلی رو بر اساس شخص پیدا کنن
    @GetMapping("/getByUser")
    fun getByUser(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam numberKarbar:String
    ): ServiceResponse<Canceling> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
               val data = service.getByCancelingMe(token,numberKarbar)
                ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

}