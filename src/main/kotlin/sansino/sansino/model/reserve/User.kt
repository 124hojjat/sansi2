package sansino.sansino.model.reserve

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.genderStatus


@Entity
@Table(name = "app_user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var name :String = "",
    var numberPhone:String = "",
//    سالن دار و اشراقی پسورد دارن بقیه با otp وارد میشن و این اصلا براشون کار نمیکنه
    var password:String? = null,
    var gender:genderStatus = genderStatus.NON,
    var role: Role? = null,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var reservation :List<Reservation>? = null,

    // سالن‌هایی که این کاربر صاحبشه (فقط وقتی role = HALL_OWNER هست)
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonIgnore
    var halls: List<Salons>? = null
)




