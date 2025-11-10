package sansino.sansino.servise.reserve

import com.kavenegar.sdk.KavenegarApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.KavenegarService
import sansino.sansino.controler.ReservationWebSocketController
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.reports.Reports
import sansino.sansino.model.reserve.Transactions
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.ReservationRepository
import sansino.sansino.repository.reserve.SlotTimeRepository
import sansino.sansino.repository.reserve.TransactionRepository
import java.util.*
import kotlin.math.log

@Service
class TransActionService(private var kavenegar: KavenegarService) {
    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository

    @Autowired
    private lateinit var slotTimeRepository: SlotTimeRepository

    @Autowired
    private lateinit var reservationWebSocketController: ReservationWebSocketController

    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    @Autowired
    private lateinit var reservationService: ReservationService

    //    ساخت یک درگاه پرداخت خاص برای کاربر
    @Transactional
    fun initiatePayment(reservationId: Long, amount: String): Transactions {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalStateException("رزرو با شناسه $reservationId یافت نشد") }
        val transaction = Transactions(
            amount = amount,
            status = ReservationStatue.PENDING,
            reservation = reservation
        )
        val saved = transactionRepository.save(transaction)
        reservation.transactions = saved
        reservationRepository.save(reservation)

        //        شماره تراکنش رو توی گزارش ذخیره میکنیم
        val report = reportsRepository.findByReservationId(reservationId)
        if (reservation.id == report.reservationId) {
            report.transactionId = transaction.id
            report.transactionStatus = transaction.status!!
            reportsRepository.save(report)
        } else throw ExceptionMe("خطا: رزور با گزارش همخوانی ندارد")

        return saved
    }

    //    بعد از اینکه کاربر پرداخت کرد این اجرا میشه که ببینه پول واریز شده یا نه اگه
//    اره که خب میاد به دیتابیس اضافه میکنه و اگر نه هم که یکی از رزرو ها کم میکنه
//    استفاده در کنترلر
    @Transactional
    fun handlePaymentCallback(transactionRef: String, success: Boolean) {
//       transactionRef  اول تراکنش رو بر اساس پیدا میکنیم
        val transaction = transactionRepository.findByTransactionRef(transactionRef)
            ?: throw IllegalStateException("Transaction not found")

        val reservation = transaction.reservation ?: throw IllegalStateException("Reservation not found")

        if (success) {
            var code: String
            do {
                code = (10000..99999).random().toString()
            } while (reservationRepository.existsByTicketCode(code))

            transaction.status = ReservationStatue.CONFIRMED
            reservation.status = ReservationStatue.CONFIRMED
            reservation.ticketCode = code
            reservationRepository.save(reservation)
            transactionRepository.save(transaction)
//            todo :اینجا باید تست بشه چون ممکنه اشتباه در بیاد
//            if (transaction.reservation?.id == null) throw ExceptionMe("مشکل در ارتباط بین رزرو و تراکنش")

//            ثبت در گزارش
            val report = reportsRepository.findByReservationId(reservation.id)
            report.transactionStatus = transaction.status!!
            report.paymentMethod = PaymentMethod.ONLINE
            report.reservationStatus = reservation.status
            reportsRepository.save(report)
//            اطلاع به سالن دار
            val salon = transaction.reservation?.slotTime?.salons?.name
            val sizeSlot = transaction.reservation?.slotTime?.capasity
            val date = transaction.reservation?.slotTime?.date
            val slotTimeStart = transaction.reservation?.slotTime?.startTime
            val slotTimeEnd = transaction.reservation?.slotTime?.endTime
            val numberSalonDar = transaction.reservation?.slotTime?.salons?.owner?.numberPhone
            val numberRezerver = transaction.reservation?.user?.numberPhone
            if (sizeSlot != null && sizeSlot > 1) {
                kavenegar.send(
                    text = "رزرو سالن $salon \n تاریخ $date زمان $slotTimeStart " +
                            " تا $slotTimeEnd توسط $numberRezerver " +
                            "رزرو شد برای مشاهده جزئیات به اپلیکیشن مراجعه کنید ",
                    numberSalonDar
                )
            }

//            اطلاع به کاربران دیگر
            reservationWebSocketController.notifyReservationConfirmed(reservation)
        } else {
            transaction.status = ReservationStatue.CANCELLED
            reservation.status = ReservationStatue.CANCELLED
            reservation.slotTime?.apply {
                onlineCount = (onlineCount - 1).coerceAtLeast(0)
                capasity += 1
                slotTimeRepository.save(this)
            }
            transactionRepository.save(transaction)
            reservationRepository.save(reservation)
            reservationWebSocketController.notifyReservationCancelled(reservation)
        }
    }

    //    گرفتن کل تراکنش های انجام شده
    fun getAll(pageIndex: Int, pageSize: Int): Page<Transactions> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(""))
        val data = transactionRepository.findAll(pageRequest)
        return data
    }

    fun findByAuthority(authority: String): Transactions? {
        return transactionRepository.findByTransactionRef(authority)
    }

    @Transactional
    fun updateTransactionRef(transactionId: Long, authority: String) {
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { IllegalStateException("Transaction not found for ID $transactionId") }

        transaction.transactionRef = authority
        transactionRepository.save(transaction)
    }

}