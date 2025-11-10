package sansino.sansino.repository.reserve

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import sansino.sansino.model.enums.StakhrOrSalon
import sansino.sansino.model.reserve.Salons
import sansino.sansino.model.reserve.User


@Repository
interface SalonsRepository : JpaRepository<Salons, Long> {


    //      سالن های یک صاحب
    @Query(
        """
    select m
    from Salons m
    join m.owner u
    where u.id = :id
    """
    )
    fun findSalonsByOwnerId(id: Long): List<Salons>


    //      صاحب یک سالن
    @Query(
        """
    select u
    from Salons m
    join m.owner u
    where u.numberPhone = :numberPhone
    """
    )
    fun findOwnerByPhone(numberPhone: String): User?


    override fun findAll(pageable: Pageable): Page<Salons>


    //      سالن های یک صاحب
    @Query(
        """
    select m
    from Salons m
    join m.activitis u
    where u.id = :id
    """
    )
    fun findSalonsByActivitis(id: Long): List<Salons>

    fun existsByNumberPhone(numberPhone: String): Boolean

    fun findAllByStakhrorsalon(stakhrOrSalon: StakhrOrSalon, pageable: Pageable): Page<Salons>

}