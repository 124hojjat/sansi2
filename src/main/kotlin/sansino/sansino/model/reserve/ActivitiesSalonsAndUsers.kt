package sansino.sansino.model.reserve

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*


//هر سالن چه کار هایی میتونه انجام بده
//مثل والیبال و فوتبال و فوتسال برای یک سالن ثبت میشه
@Entity
data class ActivitiesSalonsAndUsers(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var name: String = "...",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id")
    @JsonIgnore
    var salon: Salons? = null,
    var amount: String = ""
)

