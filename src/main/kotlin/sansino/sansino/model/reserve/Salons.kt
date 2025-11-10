package sansino.sansino.model.reserve

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import sansino.sansino.model.enums.StakhrOrSalon


@Entity
data class Salons(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var name: String = "",
    var numberPhone: String = "",
    var password: String = "",
    var address: String = "",
    var filmurl: String = "",
    var betweenWomanMan: Boolean = false,
    var stakhrorsalon :StakhrOrSalon? = null,


    @OneToMany(mappedBy = "salons", fetch = FetchType.LAZY)
    var slotTimes: MutableList<SlotTime> = mutableListOf(),
    @OneToMany(mappedBy = "salon", fetch = FetchType.LAZY)
    var imageurls: MutableList<ImagesSalon> = mutableListOf(),

    @OneToMany(mappedBy = "salon", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var activitis: MutableList<ActivitiesSalonsAndUsers> = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "owner_id")
    var owner: User? = null,

    @Embedded
    var dailySlotConfig: DailySlotConfig? = null

)
