package sansino.sansino.repository.reserve

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import sansino.sansino.model.reserve.Transactions

@Repository
interface TransactionRepository : JpaRepository<Transactions, Long>{

    fun findByTransactionRef(ref: String): Transactions?

}