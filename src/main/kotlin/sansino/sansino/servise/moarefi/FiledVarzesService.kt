package sansino.sansino.servise.moarefi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.Role
import sansino.sansino.model.moarefi.filedVarzes
import sansino.sansino.repository.moarefi.FiledVarzesRepository
import sansino.sansino.repository.reserve.UserRepository

@Service
class FiledVarzesService {


    @Autowired private lateinit var repository: FiledVarzesRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var jwt: JwtTokenUtils


    //    اضافه کردن فیلد ورزشی
    fun insert(token: String, filedVarzes: filedVarzes): filedVarzes {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")
        return repository.save(filedVarzes)
    }

    fun delete(token: String, filedVarzesId:Long) :Boolean{
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")
        val data = repository.findById(filedVarzesId).get()
        repository.delete(data)
        return true
    }

    fun getAll():List<filedVarzes>{
        return repository.findAll()
    }


}