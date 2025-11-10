package sansino.sansino.servise.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.CancelingStatus
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.Role
import sansino.sansino.model.reserve.Canceling
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.CancelingRepository
import sansino.sansino.repository.reserve.ReservationRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import sansino.sansino.servise.report.ReportServise
import java.time.LocalDateTime
import java.time.LocalTime


@Service
class CancelingService {


    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var reportServise: ReportServise

    @Autowired
    private lateinit var cancelingRepository: CancelingRepository

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var salonsRepository: SalonsRepository


    //    کاربر میخواد کنسل کنه
    fun addCanceling(token: String, reservationId: Long, refundMethod: String): Canceling {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
//        پیدا کردن رزروی که میخواد کنسل کنه
        val reservation = reservationRepository.findById(reservationId).get()
//        کنسل کننده فقط خود یوزر باشه
        if (reservation.user.id != userId) throw ExceptionMe("کنسل کردن رزرو فقط از طریق خود یوزر ها انجام می شود")
//        وضعیت رزروی که میخواد کنسل کنه
        if (reservation.status == ReservationStatue.PENDING) throw ExceptionMe("بلیط شما هنوز تائید یا پرداخت نشده است")
        if (reservation.status == ReservationStatue.CANCELLED) throw ExceptionMe("بلیط شما قبلا کنسل شده است")
        if (reservation.status == ReservationStatue.COMPLETED) throw ExceptionMe("شما بلیط را مصرف کرده اید")
        if (reservation.status == ReservationStatue.EXPIRED) throw ExceptionMe("زمان استفاده از بلیط گذشته و قابل لغو نیست")
        if (reservation.status == ReservationStatue.CONFIRMEDOFFLINE) throw ExceptionMe("این بلیط به صورت حضوری دریافت شده است از اینجا قابل لغو نیست")

        val now = LocalDateTime.now()

        val startDateTime = LocalDateTime.of(reservation.slotTime?.date, reservation.slotTime?.startTime)
        if (startDateTime.isBefore(now)) {
            throw ExceptionMe("زمان استفاده از این بلیط به پایان رسیده است")
        }

        reservation.status = ReservationStatue.CANCELLED
        reservationRepository.save(reservation)
        val dataR = reportServise.findByReservationId(reservation.id)
        dataR.reservationStatus = ReservationStatue.CANCELLED
        val dataFinal = cancelingRepository.save(
            Canceling(
                amount = dataR.amount,
                refundmethod = refundMethod,
                status = CancelingStatus.PENDING,
                trackingCode = null,
                name = dataR.userName, dateCanceling = LocalDateTime.now()
            )
        )
        reservation.cancel = dataFinal
        reservationRepository.save(reservation)
        reportsRepository.save(dataR)
        return dataFinal
    }

    //    ادمین میخواد کنسلی ها رو پرداخت کنه
    fun adminCanceling(token: String, reservationId: Long, cancelingId: Long, trackingCode: String): Canceling {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")

        val reservation = reservationRepository.findById(reservationId).get()
        val canceling = cancelingRepository.findById(cancelingId).get()
        if (canceling.id != reservation.id) throw ExceptionMe(
            "مشکل در تطابق رزرو ها" +
                    " لطفا با تیم برنامه نویس ارتباط بگیرید"
        )
        canceling.datePaiding = LocalDateTime.now()
        canceling.trackingCode = trackingCode
        val data = cancelingRepository.save(canceling)
        return data
    }

    //    ادمین میخواد کنسلی ها رو ببینه
    fun getAll(token: String): MutableList<Canceling> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (user.role != Role.ADMIN) throw ExceptionMe("شما به این گزینه دسترسی ندارید")
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        val data = cancelingRepository.findAll()
        return data
    }


    //    ادمین و سالن دار میخوان بر اساس سالن های مختلف کنسلی ها رو ببینن
    fun getBySalons(token: String, salonsId: Long): List<Canceling> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        val existingSalon = salonsRepository.findById(salonsId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }
        if (user.role != Role.ADMIN && existingSalon.owner?.id != user.id) {
            throw ExceptionMe("شما اجازه دسترسی ندارید")
        }
        val data = cancelingRepository.findAllByReservationSalon(salonsId)
        if (data.isEmpty()) return emptyList()
        return data
    }


    //    کاربر میخواد کنسلی های خودشو ببینه
    fun getByCancelingMe(token: String, numberGetAdmin: String): List<Canceling> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
//        بررسی میکنیم که کی داره کنسلی های یک یوزر رو ببینه اگه
//        ادمین بود بر اساس شماره پیداش کنه اگر هم خودش بود بر اساس ایدی یوزر
        if (user.role == Role.ADMIN) {
            val karbar = userRepository.findByNumberPhone(numberGetAdmin)
                ?: throw ExceptionMe("چنین شماره ای برای کاربر وجود ندارد")
            val data = cancelingRepository.findAllByReservationUser(karbar.id)
            return data
        } else {
            val data = cancelingRepository.findAllByReservationUser(userId = userId)
            return data
        }

    }


}