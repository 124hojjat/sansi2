package sansino.sansino.model.moarefi

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import sansino.sansino.model.reserve.ImagesSalon


@Entity
data class MoarefiSalons(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var name: String = "",
    var numberPhone: String = "",
//    رشته
    @Column(columnDefinition = "TEXT")
    var descriptionMan: String = "",
    @Column(columnDefinition = "TEXT")
    var descriptionWoman: String = "",
    @Column(columnDefinition = "TEXT")
    var secuesesfullMan: String = "",
    @Column(columnDefinition = "TEXT")
    var secuesesfullWoman: String = "",
//    راه ارتباطی
    @Column(columnDefinition = "TEXT")
    var communicationMan: String = "",
    @Column(columnDefinition = "TEXT")
    var communicationWoman: String = "",

    @ManyToOne(
        cascade = [CascadeType.PERSIST, CascadeType.MERGE],
        fetch = FetchType.LAZY
    )
    var filedVarzesh: filedVarzes? = null,

    @OneToMany(mappedBy = "moarefiSalon_Id", fetch = FetchType.LAZY)
    var imageurls: MutableList<ImagesSalon> = mutableListOf(),


    )
