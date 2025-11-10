package sansino.sansino.model.reserve

import jakarta.persistence.*
import sansino.sansino.model.enums.ReservationStatue

@Entity
data class Transactions(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var amount: String = "",
//     زیاد فکر نکنم مهم باشه
    var status: ReservationStatue? = null,
//    کد پیگیری
    var transactionRef :String = "",
    @OneToOne(mappedBy = "transactions")
    var reservation: Reservation? = null

)
