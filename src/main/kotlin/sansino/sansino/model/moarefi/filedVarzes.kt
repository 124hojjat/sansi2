package sansino.sansino.model.moarefi

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import sansino.sansino.model.reserve.ImagesSalon

@Entity
data class filedVarzes(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var name: String = "",
    var imageSalon: String? = null,

    @OneToMany(mappedBy = "filedVarzesh", fetch = FetchType.LAZY)
    @JsonIgnore
    var moarefiSalons:List<MoarefiSalons>? = null
)
