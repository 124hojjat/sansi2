package sansino.sansino.servise.reserve

import com.kavenegar.sdk.KavenegarApi
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import sansino.sansino.model.reserve.DailySlotConfig
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.components.KavenegarService
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.SlotTimeStatus
import sansino.sansino.model.reports.Reports
import sansino.sansino.model.reserve.SalonHoliday
import sansino.sansino.model.reserve.Salons
import sansino.sansino.model.reserve.SlotTime
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.SalonHolidayRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.SlotTimeRepository
import sansino.sansino.repository.reserve.UserRepository
import java.time.LocalDate
import java.time.LocalTime

@Service
class SlotTimeService(private val kavenegar: KavenegarService) {


    @Autowired
    private lateinit var slotTimeRepository: SlotTimeRepository

    @Autowired
    private lateinit var salonsRepository: SalonsRepository

    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var salonHolidayRepository: SalonHolidayRepository

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    // هر شب اجرا می‌شود (مثلاً ساعت 00:00)
    @Scheduled(cron = "0 0 0 * * *")
    fun generateNext7DaysSlots() {
        val salons = salonsRepository.findAll()
        val today = LocalDate.now()

        for (salon in salons) {
            val config = salon.dailySlotConfig
            if (config == null) {
                println("⚠️ تنظیمات تایم سالن ${salon.name} پر نشده، رد شد.")
                if (salon.owner?.numberPhone != null){
                    kavenegar.send(
                        text = "سانس های ${salon.name} تنظیم نشده است لطفا نسبت به تنظیم سانس ها در اپلیکیشن سانسینو اقدام نمایید.\nبا تشکر ",
                        salon.owner?.numberPhone!!
                    )
                }
                continue
            }

            val holidays = salonHolidayRepository.findBySalonAndDateAfter(salon, today.minusDays(1))
                .map { it.date }.toSet()

            for (i in 0 until 7) {
                val date = today.plusDays(i.toLong())
                if (date in holidays) continue
                if (slotTimeRepository.existsBySalonsAndDate(salon, date)) continue
                generateDailySlots(salon, date, config)
            }
        }
    }

    private fun generateDailySlots(salon: Salons, date: LocalDate, config: DailySlotConfig) {
        if (config.durationMinutes <= 0) {
            throw ExceptionMe("مدت زمان سانس باید بیشتر از صفر باشد")
        }

        val start = LocalTime.of(config.startHour, config.startMinute)
        val end = LocalTime.of(config.endHour, config.endMinute)
        if (!end.isAfter(start)) {
            throw ExceptionMe("ساعت پایان باید بعد از ساعت شروع باشد")
        }

        var current = start
        val slots = mutableListOf<SlotTime>()

        while (current.isBefore(end)) {
            val next = current.plusMinutes(config.durationMinutes.toLong())
            if (next.isAfter(end)) break  // تا از سانس آخر رد نشه

            slots.add(
                SlotTime(
                    status = SlotTimeStatus.AVAILABLE,
                    capasity = config.capacity,
                    onlineCount = 0,
                    offlineCount = 0,
                    date = date,
                    startTime = current,
                    endTime = next,
                    salons = salon
                )
            )

            current = next
        }

        slotTimeRepository.saveAll(slots)
    }

    // حذف یک تایم سالن
    fun deleteSlotTime(token: String, slotId: Long): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }

        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        val slot = slotTimeRepository.findById(slotId)
            .orElseThrow { ExceptionMe("سانس پیدا نشد") }

        val salon = slot.salons

        // بررسی دسترسی: فقط ادمین یا صاحب سالن
        if (user.role != Role.ADMIN && salon.owner?.id != user.id) {
            throw ExceptionMe("شما اجازه حذف این تایم را ندارید")
        }

        // بررسی اینکه رزرو دارد یا نه
        val totalReservations = (slot.offlineCount) + (slot.onlineCount)
        if (totalReservations > 0) {
            throw ExceptionMe("این سانس رزرو شده و قابل حذف نیست")
        }

        slotTimeRepository.delete(slot)
        return true
    }

    //    ادیت تایم سالن ها
    @Transactional
    fun editTimeSalons(
        slotTimeId: Long,
        numberPhoneOwner: String,
        status: SlotTimeStatus,
    ) {
        // پیدا کردن سانس
        val slot = slotTimeRepository.findById(slotTimeId)
            .orElseThrow { ExceptionMe("سانس پیدا نشد") }

        // بررسی مالکیت سالن
        if (slot.salons.owner?.numberPhone != numberPhoneOwner) {
            throw ExceptionMe("این سانس متعلق به شما نیست")
        }

        // بررسی منطق تغییر وضعیت
        when (status) {
            SlotTimeStatus.AVAILABLE -> {
                if (slot.status == SlotTimeStatus.AVAILABLE) {
                    throw ExceptionMe("این زمان از قبل در دسترس قرار دارد")
                }
                if (slot.status == SlotTimeStatus.IN_PROGRESS) {
                    throw ExceptionMe("این زمان در حال رزرو شدن است و قابل تغییر نیست")
                }
                if (slot.status == SlotTimeStatus.FULL) {
                    throw ExceptionMe("این زمان پر شده و نمی‌توانید دوباره آزاد کنید")
                }
                if (slot.status == SlotTimeStatus.CANCELLED) {
                    // سالن‌دار می‌تواند دوباره فعالش کند
                    slot.status = SlotTimeStatus.AVAILABLE
                }
            }

            SlotTimeStatus.FULL -> {
                if (slot.offlineCount + slot.onlineCount < slot.capasity) {
                    throw ExceptionMe("هنوز ظرفیت باقی مانده، نمی‌توانید وضعیت را FULL کنید")
                }
                slot.status = SlotTimeStatus.FULL
            }

            SlotTimeStatus.CANCELLED -> {
                if (slot.offlineCount + slot.onlineCount > 0) {
                    throw ExceptionMe("این زمان رزرو دارد و قابل لغو نیست")
                }
                slot.status = SlotTimeStatus.CANCELLED
            }

            SlotTimeStatus.IN_PROGRESS -> {
                throw ExceptionMe("وضعیت IN_PROGRESS را سیستم باید مدیریت کند نه سالن‌دار")
            }
        }

        slotTimeRepository.save(slot)
    }

    //    ادیت روزانه تایم سالن ها
    @Transactional
    fun editDaySlots(
        salonId: Long,
        numberPhoneOwner: String,
        date: LocalDate,
        newStatus: SlotTimeStatus
    ) {
        // پیدا کردن سالن
        val salon = salonsRepository.findById(salonId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }

        if (salon.owner?.numberPhone != numberPhoneOwner) {
            throw ExceptionMe("این سالن متعلق به شما نیست")
        }

        // گرفتن همه‌ی اسلات‌های اون روز
        val slots = slotTimeRepository.findBySalonsAndDate(salon, date)

        if (slots.isEmpty()) {
            throw ExceptionMe("هیچ تایمی برای این روز ثبت نشده است")
        }

        slots.forEach { slot ->
            when (newStatus) {
                SlotTimeStatus.CANCELLED -> {
                    if (slot.offlineCount + slot.onlineCount > 0) {
                        throw ExceptionMe("بعضی تایم‌ها رزرو شده‌اند و قابل لغو نیستند")
                    }
                    slot.status = SlotTimeStatus.CANCELLED
                }

                SlotTimeStatus.AVAILABLE -> {
                    if (slot.status == SlotTimeStatus.CANCELLED) {
                        slot.status = SlotTimeStatus.AVAILABLE
                    }
                }

                SlotTimeStatus.FULL -> {
                    if (slot.offlineCount + slot.onlineCount >= slot.capasity) {
                        slot.status = SlotTimeStatus.FULL
                    }
                }

                SlotTimeStatus.IN_PROGRESS -> {
                    throw ExceptionMe("وضعیت IN_PROGRESS فقط توسط سیستم مدیریت می‌شود")
                }
            }
        }

        slotTimeRepository.saveAll(slots)
    }

    //    رزرو های افلاین
    @Transactional
    fun editOfflineReservation(
        token: String,
        slotTimeId: Long,
        increase: Int = 0,
        reduction: Int = 0,
        numberPhoneReserver: String,
        usernameReserver: String
    ): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        val slot = slotTimeRepository.findById(slotTimeId)
            .orElseThrow { ExceptionMe("سانس پیدا نشد") }
        if (slot.salons.owner?.id != userId) throw ExceptionMe("شما سالن دار نیستید")


        // افزایش رزرو حضوری
        if (increase > 0) {
            if ((slot.offlineCount + slot.onlineCount + increase) > slot.capasity) {
                throw ExceptionMe("ظرفیت سانس پر است")
            }
            slot.offlineCount += increase

            reportsRepository.save(
                Reports(
                    numberPhone = numberPhoneReserver,
                    reservationId = null,
                    transactionId = null,
                    userId = null,
                    timeSlotId = slot.id,
                    date = slot.date,
                    hallId = slot.salons.id,
                    userName = usernameReserver,
                    hallName = slot.salons.name,
                    reservationStatus = ReservationStatue.CONFIRMEDOFFLINE,
                    amount = "حضوری",
                    paymentMethod = PaymentMethod.Presence,
                    transactionStatus = ReservationStatue.CONFIRMEDOFFLINE
                )
            )
        }


        // کاهش رزرو حضوری
        if (reduction > 0) {
            if (slot.offlineCount - reduction < 0) {
                throw ExceptionMe(" تعداد رزرو حضوری نمی‌تواند منفی شود و نمیتوانید رزرو های انلاین را کنسل کنید")
            }
            val reserveReports = reportsRepository.findByTimeSlotIdAndNumberPhone(
                slotTime = slotTimeId,
                numberPhone = numberPhoneReserver
            )
            if (reserveReports.isEmpty()) throw ExceptionMe("چنین شماره ای قبلا رزرو حضوری انجام نداده است که بخواهید کنسل کنید")
            reserveReports[0].reservationStatus = ReservationStatue.CANCELLED
            slot.offlineCount -= reduction
        }

        // آپدیت وضعیت
        slot.status = when {
            (slot.offlineCount + slot.onlineCount) >= slot.capasity -> SlotTimeStatus.FULL
            slot.offlineCount + slot.onlineCount == 0 -> SlotTimeStatus.AVAILABLE
            else -> SlotTimeStatus.AVAILABLE // میشه PARTIALLY_BOOKED هم بذاری
        }

        slotTimeRepository.save(slot)
        return true
    }

    //    گرفتن کل تایم های یک سالن خاص
    fun getBySalonsId(salonId: Long, pageIndex: Int, pageSize: Int): List<SlotTime> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("date"))
        val data = slotTimeRepository.findAllBySalonsId(salonId, pageRequest)
        return data
    }

    //    گرفتن کل تایم های یک سالن در یک روز خاص
    fun getBySalonsAndDate(salonsId: Long, dateTime: LocalDate, pageSize: Int, pageIndex: Int): List<SlotTime> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("startTime"))
        val data = slotTimeRepository.findBySalonsIdAndDate(salonsId, dateTime, pageRequest)
        return data.content.toList()
    }

    //    کانفیگ تایم های روزانه
    fun dailyConfig(token: String, salonId: Long, config: DailySlotConfig): DailySlotConfig? {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        val salon = salonsRepository.findById(salonId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }
//        if (salon.owner?.id != userId) throw ExceptionMe("شما سالن دار نیستید")
        salon.dailySlotConfig = config
        salonsRepository.save(salon)
        return salon.dailySlotConfig
    }


    //    افزودن روز تعطیل
    fun addHoliday(token: String, salonId: Long, date: String): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        val salon = salonsRepository.findById(salonId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }
        if (salon.owner?.id != userId && user.role != Role.ADMIN) throw ExceptionMe("شما سالن دار نیستید")
        val localDate = LocalDate.parse(date)
        // گرفتن همه‌ی اسلات‌های اون روز
        val slots = slotTimeRepository.findBySalonsAndDate(salon, localDate)
        slots.forEach { slot ->
            if (slot.offlineCount + slot.onlineCount > 0) throw ExceptionMe("سانس ${slot.startTime}تا ${slot.endTime}رزور شده است نمیتوان این روز را حذف یا تعطیل کرد")
            slotTimeRepository.delete(slot)
        }
        if (salonHolidayRepository.existsBySalonAndDate(salon, localDate))
            throw ExceptionMe("این تاریخ قبلاً به عنوان تعطیل ثبت شده است")

        salonHolidayRepository.save(SalonHoliday(date = localDate, salon = salon))
        return true
    }


    //    حذف روز تعطیل
    fun deleteHoliday(token: String, salonId: Long, date: String): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        val salon = salonsRepository.findById(salonId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }

        if (salon.owner?.id != userId) throw ExceptionMe("شما سالن دار نیستید")

        val localDate = LocalDate.parse(date)

        val holiday = salonHolidayRepository.findBySalonAndDate(salon, localDate)
            ?: throw ExceptionMe("چنین روز تعطیلی وجود ندارد")
        salonHolidayRepository.delete(holiday)
        generateNext7DaysSlots()
        return true
    }

    //    دریافت لیست تعطیلات سالن
    fun getHollyDay(salonId: Long): List<SalonHoliday> {
        val salon = salonsRepository.findById(salonId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }

        return salonHolidayRepository.findBySalon(salon)
    }


}
