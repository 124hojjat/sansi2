package sansino.sansino.components.dto

import sansino.sansino.model.reserve.ImagesSalon

data class GetSalonNameDto(
    var id:Long,
    var name :String,
    var address :String,
    var imageUrl:MutableList<ImagesSalon>
)