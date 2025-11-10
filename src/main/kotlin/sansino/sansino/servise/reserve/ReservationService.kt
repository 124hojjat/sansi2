package sansino.sansino.servise.reserve

import jakarta.persistence.OptimisticLockException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.components.dto.ReservationGoToPaymentDto
import sansino.sansino.components.dto.ReservationVerificationResponseDto
import sansino.sansino.controler.ReservationWebSocketController
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.reports.Reports
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Reservation
import sansino.sansino.model.reserve.User
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.ReservationRepository
import sansino.sansino.repository.reserve.SlotTimeRepository
import sansino.sansino.repository.reserve.UserRepository
import java.time.LocalDateTime

/*
//صدا زدن یک متد ترنزاکشن که توی همون کلاس هست خطای پروکسی میده و ممکنه باعت مشکل بشه برای جلوگیری از این مشکل باید این انوتیشن رو بزنیم
@EnableAspectJAutoProxy(exposeProxy = true)*/


@Service
class ReservationService {


    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var slotTimeRepository: SlotTimeRepository

    @Autowired
    private lateinit var reservationWebSocketController: ReservationWebSocketController

    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository


    // استفاده در خود همین سرویس
    //ساخت یک رزرو موقت
    @Transactional(/*propagation = Propagation.REQUIRES_NEW*/)
    fun createTemporaryReservation(
        slotTimeId: Long,
        user: User,
        gender: genderStatus = genderStatus.NON,
        activitiesSalonsAndUsers: Long
    ): ReservationGoToPaymentDto {
        val currentGender = if (gender == genderStatus.NON) user.gender else gender
        if (currentGender == genderStatus.NON) throw ExceptionMe("جنسیت مشخص نیست")
//         باید بررسی بشه ایا رزروی کرده برای این تایم یا نه
//         اگه رزرو کرده دو تا حالت داره یا
//         1 پرداخت کرده 2 نکرده
//          در هر صورت ادامه پیدا نمیکنه چون رزرو داره
//          اگه رزرو رو پرداخت کرده میگیم شما قبلا پرداخت کردی
//          ولی اگه پرداخت نکرده رزرو موقت رو بر میگردونیم و میگیم همون رو پرداخت کنه

        val slotTime = slotTimeRepository.findById(slotTimeId).orElseThrow()
//        todo :یه رفع باگ همینطوری انجام دادم اونم اینکه ظرفیت به صفر که میرسید باز هم میتونستن رزرو کنن تایم رو برای همین اول بررسی میکنیم که تایم ظرفیتش چقدریه
        if (slotTime.capasity == 0) throw ExceptionMe("بلیط این تایم به فروش رفته ")
        val pastReservation = reservationRepository.findAllByUserAndSlotTime(user, slotTime)
        val activitySalon = slotTime.salons.activitis
        val amount = activitySalon
            .find { it.id == activitiesSalonsAndUsers }
            ?.amount ?: activitySalon.first().amount

        for (item in pastReservation) {
            if (item.status == ReservationStatue.COMPLETED) throw ExceptionMe("شما قبلاً این سانس را پرداخت کرده‌اید.")
            if (item.status == ReservationStatue.PENDING) return ReservationGoToPaymentDto(
                reservationId = item.id,
                salonName = item.slotTime?.salons?.name ?: "",
                date = slotTime.date,
                startTime = slotTime.startTime,
                endTime = slotTime.endTime,
                amount = amount,
                gender = item.gender,
                name = item.user.name,
            )
        }

//        پیاده سازی اینکه بین دو تا سالن خانم نشه یک سالن اقا انتخاب کرد و برعکس
        if (slotTime.salons.betweenWomanMan) {
//            شروع یک سالن اخر سالن قبلی هست دیگه :)
            val beforSlotTime = slotTimeRepository.findBySalonsAndDateAndEndTime(
                slotTime.salons,
                date = slotTime.date,
                slotTime.startTime
            )
//            اخر یک سالن شروع سالن بعدی هست دیگه :)
            val nextSlotTime = slotTimeRepository.findBySalonsAndDateAndStartTime(
                slotTime.salons,
                date = slotTime.date,
                slotTime.endTime
            )
//            امن کردن وضعیت سالن های قبلی و بعدی
            val beforeGender = beforSlotTime?.reservations?.firstOrNull()?.gender
            val nextGender = nextSlotTime?.reservations?.firstOrNull()?.gender
            val currentGender = if (gender == genderStatus.NON) user.gender else gender
            if (beforeGender != null && nextGender != null) {
                if (beforeGender == nextGender && nextGender != currentGender) {
                    throw ExceptionMe(
                        "طبق قوانین سالن ${slotTime.salons.name} شما نمیتوانید بین دو سانس ${
                            beforSlotTime.reservations?.get(
                                0
                            )?.gender
                        } سانس ${slotTime.reservations?.get(0)?.gender}انتخاب کنید"
                    )
                }
            }

        }
        if (slotTime.capasity <= 0) {
            throw IllegalStateException("ظرفیت پر شده است")
        }
        //  onlineCount  یکی اضافه کن به
        slotTime.onlineCount += 1
//        یه ظرفیت هم کم کن
        slotTime.capasity -= 1
//        ذخیره بشه
// اینجا Hibernate وقتی flush کنه، version رو چک می‌کنه
        slotTimeRepository.saveAndFlush(slotTime)
        // رزرو موقت بساز
        val reservation = Reservation(
            user = user,
            slotTime = slotTime,
            status = ReservationStatue.PENDING,
            expiresAt = System.currentTimeMillis() + 5 * 60 * 1000, // ۵ دقیقه,
            ticketCode = null,
            gender = if (gender == genderStatus.NON) user.gender else gender,
            amount = amount
        )
//        رزرو موقت رو ثبت میکنیم
        val saved = reservationRepository.save(reservation)
//        اطلاع میدیم که یک رزرو موقت انجام شده
        reservationWebSocketController.notifyReservationCreated(saved)
//        جدول گزارش شروع به پر شدن میکنه
        reportsRepository.save(
            Reports(
                reservationId = reservation.id,
                timeSlotId = slotTime.id,
                hallId = slotTime.salons.id,
                userId = user.id,
                date = slotTime.date,
                userName = user.name,
                numberPhone = user.numberPhone,
                hallName = slotTime.salons.name,
                amount = reservation.amount!!,
                paymentMethod = PaymentMethod.ONLINE,
                reservationStatus = ReservationStatue.PENDING,
                transactionStatus = ReservationStatue.PENDING
            )
        )
        return ReservationGoToPaymentDto(
            reservationId = saved.id,
            salonName = saved.slotTime?.salons?.name ?: "",
            date = slotTime.date,
            startTime = slotTime.startTime,
            endTime = slotTime.endTime,
            amount = amount,
            gender = saved.gender,
            name = saved.user.name
        )
    }

    // نیاز به کنترلر نداره
    //این با scachuled هر یک دقیقه اجرا میشه و اونایی که تایمشون گذشته رو پاک میکنه
//    هم برای رزرو های اولیه و پرداخت نشده کار میکنه و هم برای رزرو های انجام شده که تایمشون گذشته
    @Transactional
    fun cancelExpiredReservations() {

        println("⏰Scheduled")
//        تایم رو بگیر
        val now = System.currentTimeMillis()  // millis
//    ببین وقتش گذشته یا نه
        val expired = reservationRepository.findByStatusAndExpiresAtBefore(
            ReservationStatue.PENDING, now
        )
        expired.forEach { reservation ->
            reservation.status = ReservationStatue.CANCELLED
            reservation.slotTime?.apply {
                onlineCount = (onlineCount - 1).coerceAtLeast(0)
                capasity += 1
                slotTimeRepository.save(this)
            }
            val report = reportsRepository.findByReservationId(reservation.id)
            report.reservationStatus = reservation.status
            reservationRepository.save(reservation)

            //  اطلاع‌رسانی به کلاینت‌ها
            reservationWebSocketController.notifyReservationCancelled(reservation)
        }
        // 🔹 بخش ۲: رزروهایی که تایید شدن ولی زمان استفاده‌شون گذشته (مثلاً سالن تموم شده)
        val confirmedReservations = reservationRepository.findByStatus(ReservationStatue.CONFIRMED)
        val nowLocal = LocalDateTime.now()

        confirmedReservations.forEach { reservation ->
            val slot = reservation.slotTime ?: return@forEach
            val endDateTime = LocalDateTime.of(slot.date, slot.endTime)

            if (endDateTime.isBefore(nowLocal)) {
                reservation.status = ReservationStatue.EXPIRED
                reservationRepository.save(reservation)
                println("⏰ Reservation ${reservation.id} expired (slot ended)")
            }
        }
    }



    //    گرفتن رزرو های من
    fun getMyReserve(token: String): List<Reservation> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.KARBAR) throw ExceptionMe("نقش ادمین و سالن دار رزروی ندارند")
        val data = reservationRepository.findAllByUser(user)
        return data
    }

    //    برای کار با optimistic هست
    fun tryCreateReservation(
        slotTimeId: Long,
        token: String,
        gender: genderStatus,
        activitiesSalonsAndUsers: Long
    ): ReservationGoToPaymentDto {
        repeat(3) { attempt ->
            try {
                val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
                val user = userRepository.findById(userId)
                    .orElseThrow { ExceptionMe("کاربر یافت نشد") }
                if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
                return createTemporaryReservation(
                    slotTimeId,
                    user,
                    gender = gender,
                    activitiesSalonsAndUsers = activitiesSalonsAndUsers
                )
            } catch (ex: OptimisticLockException) {
                println("Optimistic lock error! retry $attempt ...")
                Thread.sleep(100) // کمی صبر می‌کنیم و دوباره تلاش
            }
        }
        throw IllegalStateException("رزرو به دلیل تداخل همزمانی انجام نشد")
    }


    fun verifyReservation(token: String, ticketCode: String): ReservationVerificationResponseDto {
//        وضعیت توکن
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
//        وضعیت کد
        val reservation = reservationRepository.findByTicketCode(ticketCode)
            ?: throw ExceptionMe("رزرو یافت نشد")

        val report = reportsRepository.findByReservationId(reservation.id)

        val now = LocalDateTime.now()
        val start = LocalDateTime.of(reservation.slotTime?.date, reservation.slotTime?.startTime)
        val end = LocalDateTime.of(reservation.slotTime?.date, reservation.slotTime?.endTime)

        // وضعیت‌های نامعتبر
        if (reservation.status in listOf(
                ReservationStatue.CANCELLED,
                ReservationStatue.EXPIRED,
            )
        ) throw ExceptionMe("رزرو معتبر نیست (${reservation.status})")

        if (reservation.status == ReservationStatue.COMPLETED) throw ExceptionMe("بلیط قبلا استفاده شده است")

        // زمان استفاده گذشته؟
        if (end.isBefore(now)) {
            reservation.status = ReservationStatue.COMPLETED
            reservationRepository.save(reservation)
            throw ExceptionMe("زمان استفاده از این بلیط گذشته است")
        }

        // هنوز وقتش نرسیده؟
        if (start.isAfter(now)) {
            throw ExceptionMe("زمان رزرو هنوز شروع نشده است")
        }

        // اگر همه چیز درست بود، بلیط رو معتبر اعلام کن وضعیت رو اپدیت کن
        reservation.status = ReservationStatue.COMPLETED
        report.reservationStatus = ReservationStatue.COMPLETED
        reportsRepository.save(report)
        reservationRepository.save(reservation)

        return ReservationVerificationResponseDto(
            success = true,
            userName = reservation.user.name,
            startTime = reservation.slotTime?.startTime,
            endTime = reservation.slotTime?.endTime,
            message = "رزرو معتبر است"
        )
    }

}

