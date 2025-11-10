package sansino.sansino.model.reports

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import java.time.LocalDate
import java.time.LocalDateTime


@Entity
//برای انجام کوئری های سریع تر
@Table(
    name = "reports",
    indexes = [
        Index(name = "idx_reports_hall_id", columnList = "hallId"),
        Index(name = "idx_reports_user_id", columnList = "userId"),
        Index(name = "idx_reports_created_at", columnList = "createdAt"),
        Index(name = "idx_reports_reservation_status", columnList = "reservationStatus")
    ]
)
//این @EntityListeners(AuditingEntityListener::class) برای اینه که تایم های  updatedAt و createdAt خودکار پر بشه
@EntityListeners(AuditingEntityListener::class)
data class Reports(
//    ---------------------------------- id
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
//    ایدی رزرو
    var reservationId: Long? = null,
//    ایدی پرداخت
    var transactionId: Long? = null,
//    ایدی یوزر
    var userId: Long? = null,
//    ایدی سالن
    var hallId: Long = 0,
//    ایدی تایمی که از سالن گرفته👀
    var timeSlotId: Long = 0,
//    ----------------------------------name
//    نام رزرو کننده
    var userName: String = "",
//    شماره رزرو کننده
    var numberPhone: String = "",
//    نام سالن
    var hallName: String = "",
//    ----------------------------------date
//    تاریخ روز بلیط؟
    var date: LocalDate = LocalDate.now(),
//    بر اساس زمان ثبت این گزارش
    @CreatedDate
    var createdAt: LocalDateTime? = null,
//    اخرین اپدیت این گزارش
    @LastModifiedDate
    var updatedAt: LocalDateTime? = null,
//    ----------------------------------other
//    بر اساس وضعیت رزرو
    @Enumerated(EnumType.STRING)
    var reservationStatus: ReservationStatue = ReservationStatue.PENDING,
//    بر اساس قیمت
    var amount: String = "",
//    بر اساس روش پرداخت
    @Enumerated(EnumType.STRING)
    var paymentMethod: PaymentMethod = PaymentMethod.NON,
//    بر اساس وضعیت پرداخت
    @Enumerated(EnumType.STRING)
    var transactionStatus: ReservationStatue = ReservationStatue.PENDING,
)


