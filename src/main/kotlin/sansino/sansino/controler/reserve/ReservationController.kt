package sansino.sansino.controler.reserve

import com.google.api.services.storage.Storage.Buckets.Get
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.GetMyReservationDto
import sansino.sansino.components.dto.GetSalonNameDto
import sansino.sansino.components.dto.ReservationGoToPaymentDto
import sansino.sansino.components.dto.ReservationVerificationResponseDto
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Reservation
import sansino.sansino.model.reserve.User
import sansino.sansino.servise.reserve.ReservationService


@RestController
@RequestMapping("/api/Reservation")
class ReservationController {

    @Autowired
    private lateinit var service: ReservationService

    //      ✔
    @PostMapping("/create")
    fun create(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam slotTimeId: Long,
        @RequestParam activity:Long,
        @RequestParam gender: genderStatus = genderStatus.NON
    ): ServiceResponse<ReservationGoToPaymentDto> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.tryCreateReservation(slotTimeId, token, gender,activity)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(
                data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message =
                "${e.message}"
            )
        }
    }


    //      ✔
    @GetMapping("/getAll")
    fun getAll(
        @RequestHeader("Authorization") authHeader: String?,
    ): ServiceResponse<GetMyReservationDto> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.getMyReserve(token)
            val dataDto = data.map { item ->
                GetMyReservationDto(
                    id = item.id,
                    status = item.status,
                    amount = item.amount,
                    date = item.slotTime?.date!!,
                    startTime = item.slotTime?.startTime!!,
                    endTime = item.slotTime?.endTime!!, nameSalon = item.slotTime?.salons?.name,
                    numberPhoneKarbar = item.user.numberPhone,
                    ticketCode = item.ticketCode
                )
            }
            return ServiceResponse(data = dataDto, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //      ✔
    @GetMapping("/checkHolder")
    fun checkHolder(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam ticketCode: String
    ): ServiceResponse<ReservationVerificationResponseDto> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.verifyReservation(token, ticketCode)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }
}