package sansino.sansino.repository.moarefi

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import sansino.sansino.model.moarefi.MoarefiSalons
import java.util.*


@Repository
interface MoarefiSalonsRepository : PagingAndSortingRepository<MoarefiSalons, Long>,
    CrudRepository<MoarefiSalons, Long> {

    fun findByNumberPhone(name: String): MoarefiSalons


    fun findAllByFiledVarzeshId(
        @Param("filedVarzeshId") filedVarzeshId: Long,
        pageable: Pageable
    ): List<MoarefiSalons>


}