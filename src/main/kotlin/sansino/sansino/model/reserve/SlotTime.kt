package sansino.sansino.model.reserve

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.grammars.hql.HqlParser.MonthContext
import sansino.sansino.model.enums.PaymentMethod
import sansino.sansino.model.enums.SlotTimeStatus
import sansino.sansino.model.enums.StakhrOrSalon
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.Year

@Entity
data class SlotTime(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var status: SlotTimeStatus? = null,
    var capasity: Int = 0,
    // تفکیک رزروها
    var onlineCount: Int = 0,   // رزروهای آنلاین
    var offlineCount: Int = 0,  // رزروهای حضوری


    var date: LocalDate = LocalDate.now(),       // روز سانس
    var startTime: LocalTime = LocalTime.of(0, 0), // زمان شروع
    var endTime: LocalTime = LocalTime.of(0, 0),   // زمان پایان
//    var paymentMethod: PaymentMethod = PaymentMethod.ONLINE,
    @OneToMany(mappedBy = "slotTime", fetch = FetchType.LAZY)
    var reservations: List<Reservation>? = null,

    @ManyToOne
    @JsonIgnore
    var salons: Salons = Salons(),


    @Version
    var version: Long = 0
)

