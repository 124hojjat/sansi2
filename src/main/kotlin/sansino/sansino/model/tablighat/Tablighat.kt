package sansino.sansino.model.tablighat

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Tablighat(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id :Long = 0,
    var imageUrl :String = "",
)
