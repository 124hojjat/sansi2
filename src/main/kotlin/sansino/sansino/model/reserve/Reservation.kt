package sansino.sansino.model.reserve

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.persistence.GenerationType.AUTO
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.model.enums.genderStatus
import java.math.BigDecimal
import java.util.*

@Entity
data class Reservation(
    @Id @GeneratedValue(strategy = AUTO)
    var id: Long = 0,
    @Enumerated(EnumType.STRING)
    var status: ReservationStatue = ReservationStatue.PENDING,
    var createdAt: Long = System.currentTimeMillis(),
    var amount: String? = null,
    var paymentMethod: PaymentMethod = PaymentMethod.ONLINE,
    var gender: genderStatus = genderStatus.NON,

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    var user: User = User(),

    @OneToOne(fetch = FetchType.EAGER)
    var cancel: Canceling? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    var transactions: Transactions? = null,

    // تاریخ انقضای رزرو موقت
    var expiresAt: Long = System.currentTimeMillis() + 5 * 60 * 1000,

    @ManyToOne(fetch = FetchType.EAGER)
    var slotTime: SlotTime? = null,

//    کدی که قراره سالن دار بررسی کنه
    var ticketCode: String? = generateTicketCode(),

    @Version
    var version: Long = 0
){
    companion object {
        fun generateTicketCode(): String =
            (10000..99999).random().toString()
    }
}
