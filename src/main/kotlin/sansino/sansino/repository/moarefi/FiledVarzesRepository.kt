package sansino.sansino.repository.moarefi

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.moarefi.filedVarzes

@Repository
interface FiledVarzesRepository :JpaRepository<filedVarzes,Long>