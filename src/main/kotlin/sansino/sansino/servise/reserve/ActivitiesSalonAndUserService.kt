package sansino.sansino.servise.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Salons
import sansino.sansino.repository.moarefi.MoarefiSalonsRepository
import sansino.sansino.repository.reserve.ActivitiesSalonsAndUsersRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
/*

@Service
class ActivitiesSalonAndUserService {

    @Autowired
    private lateinit var repository: ActivitiesSalonsAndUsersRepository
    @Autowired
    private lateinit var salonsRepository: SalonsRepository

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository


    fun insert(token :String,activitiesSalonsAndUsers: ActivitiesSalonsAndUsers,salonsId :Long){
        checkToken(token)
        val salons= salonsRepository.findById(salonsId).get()
        activitiesSalonsAndUsers.salon  = salons
        repository.save(activitiesSalonsAndUsers)
    }


    fun delete(token :String,activitiesSalonsAndUsers: ActivitiesSalonsAndUsers,salonsId :Long){
        checkToken(token)
        val salons= salonsRepository.findById(salonsId).get()
        activitiesSalonsAndUsers.salon  = salons
        repository.save(activitiesSalonsAndUsers)
    }



    fun checkToken(token: String): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        return true
    }
}*/
