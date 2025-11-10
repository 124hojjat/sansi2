package sansino.sansino.model.reserve

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.*
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import sansino.sansino.model.enums.CancelingStatus
import java.math.BigDecimal
import java.time.LocalDateTime


@Entity
data class Canceling(
    @Id @GeneratedValue(strategy = AUTO)
    var id: Long = 0,
    var amount: String? =null,
//    شماره کارت بانکی
    var refundmethod: String = "",
//    وضعیت
    var status: CancelingStatus? = null,
//    شماره پیگیری که خود اشراقی بعد پرداخت پر میشه
    var trackingCode: String? = null,
    var name: String = "",
//    تاریخی که درخواست کنسلی دادن
    var dateCanceling: LocalDateTime = LocalDateTime.now(),
//    تاریخی که پول طرف رو اشراقی واریز کرد
    var datePaiding: LocalDateTime?=null,

    @OneToOne(mappedBy = "cancel")
    var reservation: Reservation? = null

)









