package sansino.sansino.model.reserve

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.persistence.GenerationType.AUTO
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.enums.whreSetImage
import sansino.sansino.model.moarefi.MoarefiSalons
import sansino.sansino.model.moarefi.filedVarzes

// todo : این تعیر میکنه و شامل  سه وضعیت میشه 1 مربوط به خانم ها و
//  اقایان 2 بر اساس مکان جایگذاری عکس که شامل
//  سه مکان میشه سردر، افتخارات و معرفی 3یه فیلد توضیحات هم باید اضافه بشه
@Entity
data class ImagesSalon(
    @Id @GeneratedValue(strategy = AUTO)
    val id: Long = 0,
    val image: String = "",
    val womanAndMan: genderStatus? = null,
    val whereSet: whreSetImage? = null,
    var tablighat :Boolean? = null,
    val description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    @JsonIgnore
    val salon: Salons? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moarefiSalon_Id")
    @JsonIgnore
    val moarefiSalon_Id: MoarefiSalons? = null,
)
