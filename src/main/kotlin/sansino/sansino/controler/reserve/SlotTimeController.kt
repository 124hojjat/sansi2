package sansino.sansino.controler.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.model.reserve.DailySlotConfig
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.SalonHolyDayDto
import sansino.sansino.components.dto.SlotTimeGetByDate
import sansino.sansino.model.reserve.SalonHoliday
import sansino.sansino.servise.reserve.SlotTimeService
import java.time.LocalDate


@RestController
@RequestMapping("/api/slotTime")
class SlotTimeController {

    @Autowired
    private lateinit var service: SlotTimeService

    //    todo: توی محیط پروداکشن حذفش کن
    @PostMapping("/generateNext7Days")
    fun generateNext7DaysManually(): String {
        service.generateNext7DaysSlots()
        return "تایم‌ها برای 7 روز آینده ساخته شدند"
    }


//    ✔
//    همه تایم های سالن ها رو بگیر
    @GetMapping("/getBySalons")
    fun getAllBySalons(
        @RequestParam salonId: Long,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int
    ): ServiceResponse<SlotTimeGetByDate> {
        try {
            val data = service.getBySalonsId(salonId = salonId, pageIndex = pageIndex, pageSize = pageSize)
            val dataList: List<SlotTimeGetByDate> = data.map { slot ->
                SlotTimeGetByDate(
                    id = slot.id,
                    status = slot.status,
                    capasity = slot.capasity,
                    date = slot.date,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    activity = slot.salons.activitis
                )
            }
            return ServiceResponse(data = dataList, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //    گرفتن تایم سالن ها بر اساس تاریخ
    @GetMapping("/getByDateSalons")
    fun getByDateSalons(
        @RequestParam salonId: Long,
        @RequestParam pageIndex: Int,
        @RequestParam dateTime: LocalDate,
        @RequestParam pageSize: Int
    ): ServiceResponse<SlotTimeGetByDate> {
        try {
            val data = service.getBySalonsAndDate(salonId, dateTime, pageSize, pageIndex)
            val dataList: List<SlotTimeGetByDate> = data.map { slot ->
                SlotTimeGetByDate(
                    id = slot.id,
                    status = slot.status,
                    capasity = slot.capasity,
                    date = slot.date,
                    startTime = slot.startTime,
                    endTime = slot.endTime,
                    activity = slot.salons.activitis
                )
            }
            return ServiceResponse(data =
            dataList, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //    ایدیت و تنظیم تایم ها /////مهم/////
    @PutMapping("/daily/{salonId}")
    fun updateDailyConfig(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable salonId: Long,
        @RequestBody config: DailySlotConfig
    ): ServiceResponse<DailySlotConfig> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.dailyConfig(token, salonId, config) ?: throw ExceptionMe("دوباره تلاش کنید")
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    // 📅 افزودن روز تعطیل
    @PostMapping("/holiday/{salonId}")
    fun addHoliday(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable salonId: Long,
        @RequestParam date: String
    ): ServiceResponse<Boolean> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.addHoliday(token, salonId, date)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }

    }

    // ❌ حذف روز تعطیل
    @DeleteMapping("/holiday/{salonId}")
    fun removeHoliday(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable salonId: Long,
        @RequestParam date: String
    ): ServiceResponse<Boolean> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
        }
        return try {
            val token = authHeader.substring(7)
            val data = service.deleteHoliday(token, salonId, date)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    // 📋 دریافت لیست تعطیلات سالن
    @GetMapping("/holiday/{salonId}")
    fun getHolidays(
        @PathVariable salonId: Long
    ): ServiceResponse<SalonHolyDayDto> {
        try {
            val data = service.getHollyDay(salonId)

            val dataList: List<SalonHolyDayDto> = data.map { slot ->
                SalonHolyDayDto(
                    id = slot.id,
                    date = slot.date,
                    salonName = slot.salon?.name ?: "بدون نام"
                )
            }
            return ServiceResponse(data = dataList, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    //    یه تایمی رو میخواد حذف کنه
    @DeleteMapping("/deleteSlot")
    fun deleteSlot(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam slotId: Long
    ): ServiceResponse<Boolean> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.deleteSlotTime(token, slotId)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    // مدیریت رزرو حضوری (افزایش یا کاهش)
    @PostMapping("/offlineReservation")
    fun manageOfflineReservation(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam slotTimeId: Long,
        @RequestParam numberPhoneOwner: String,
        @RequestParam(required = false, defaultValue = "0") increase: Int,
        @RequestParam(required = false, defaultValue = "0") reduction: Int,
        @RequestParam numberPhoneReserver: String,
        @RequestParam usernameReserver: String
    ): ServiceResponse<Boolean> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            // فراخوانی سرویس برای اعمال تغییرات
            val data = service.editOfflineReservation(
                token = token,
                slotTimeId = slotTimeId,
                increase = increase,
                reduction = reduction,
                numberPhoneReserver = numberPhoneReserver,
                usernameReserver = usernameReserver
            )
            ServiceResponse(data = listOf(data), status = HttpStatus.OK, message = "تغییرات اعمال شد")
        } catch (e: ExceptionMe) {
            ServiceResponse(status = HttpStatus.BAD_REQUEST, message = e.message ?: "خطای نامشخص")
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = e.message ?: "خطای سرور")
        }
    }
}