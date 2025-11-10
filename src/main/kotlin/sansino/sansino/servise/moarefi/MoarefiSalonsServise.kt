package sansino.sansino.servise.moarefi

import org.hibernate.engine.jdbc.Size
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.Role
import sansino.sansino.model.moarefi.MoarefiSalons
import sansino.sansino.model.moarefi.filedVarzes
import sansino.sansino.repository.moarefi.FiledVarzesRepository
import sansino.sansino.repository.moarefi.MoarefiSalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import java.util.*


@Service
class MoarefiSalonsServise {

    @Autowired
    private lateinit var moarefiSalonsRepository: MoarefiSalonsRepository
    @Autowired
    private lateinit var jwt: JwtTokenUtils
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var filedVarzesRepository: FiledVarzesRepository


    //    insert
    fun insert(token: String, moarefiSalons: MoarefiSalons, filedVarzesId:Long): MoarefiSalons {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")

        // یافتن لیست فیلدهای ورزشی
        val fields = filedVarzesRepository.findById(filedVarzesId).get()
        moarefiSalons.filedVarzesh = fields
        return moarefiSalonsRepository.save(moarefiSalons)
    }

    //    update
    fun update(token: String, moarefiSalons: MoarefiSalons, filedVarzesId:Long): MoarefiSalons {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }

        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")

        // یافتن لیست فیلدهای ورزشی
        val fields = filedVarzesRepository.findById(filedVarzesId).get()

        // پیدا کردن MoarefiSalons بر اساس شماره
        val data = getByNumberPhone(token, moarefiSalons.numberPhone)

        // آپدیت فیلدهای ساده
        if (moarefiSalons.name.isNotEmpty()) data.name = data.name
        if (moarefiSalons.communicationMan.isNotEmpty()) data.communicationMan = moarefiSalons.communicationMan
        if (moarefiSalons.descriptionWoman.isNotEmpty()) data.descriptionWoman = moarefiSalons.descriptionWoman
        if (moarefiSalons.filedVarzesh != null) data.filedVarzesh = fields

        // ذخیره‌سازی
        return moarefiSalonsRepository.save(data)
    }


    //    delete
    fun delete(token: String, moarefiSalons: MoarefiSalons): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")
        moarefiSalonsRepository.delete(moarefiSalons)
        return true
    }

    //  گرفتن بر اساس شماره تلفن
    fun getByNumberPhone(token: String, numberPhone: String): MoarefiSalons {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("شما ادمین نیستید")
        return moarefiSalonsRepository.findByNumberPhone(numberPhone)
    }

    //    گرفتن معرفی سالن ها به طور کلی
    fun getAll(pageIndex: Int, pageSize: Int): List<MoarefiSalons> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("id"))
        val data: List<MoarefiSalons> = moarefiSalonsRepository.findAll(pageRequest).content
        return data
    }


    fun getAllByField(filedVarzesId: Long, pageIndex: Int, pageSize: Int): List<MoarefiSalons> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("id"))
        val data: List<MoarefiSalons> = moarefiSalonsRepository.findAllByFiledVarzeshId(filedVarzesId, pageRequest)
        return data
    }


    //    گرفتن معرفی سالن ها بر اساس ایدی
    fun getById(id: Long): MoarefiSalons {
        val data = moarefiSalonsRepository.findById(id)
        return data.get()
    }
}