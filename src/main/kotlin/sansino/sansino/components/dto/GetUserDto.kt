package sansino.sansino.components.dto

import sansino.sansino.model.enums.genderStatus

data class GetUserDto(

    val  name:String,
    val numberPhone:String,
    val genderStatus: genderStatus

)
