package sansino.sansino.repository.reserve

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.reserve.ImagesSalon
import sansino.sansino.model.reserve.Salons

@Repository
interface ImageSalonRepository:JpaRepository<ImagesSalon,Long>{

    fun findAllBySalon(salon: Salons): List<ImagesSalon>

    fun findAllBySalonId(salonId: Long): List<ImagesSalon>?

}