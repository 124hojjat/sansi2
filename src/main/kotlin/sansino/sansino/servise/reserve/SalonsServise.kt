package sansino.sansino.servise.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.StakhrOrSalon
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Salons
import sansino.sansino.repository.reserve.ActivitiesSalonsAndUsersRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import java.util.*

@Service
class SalonsServise {

    @Autowired
    private lateinit var salonsRepository: SalonsRepository

    @Autowired
    private lateinit var userRepository: UserRepository


    @Autowired
    private lateinit var jwt: JwtTokenUtils


    //    اضافه کردن سالن توسط ادمین
    fun insert(
        token: String,
        salons: Salons,
        numberPhoneOwner: String,
        activitiesSalonsAndUsers: MutableList<ActivitiesSalonsAndUsers>
    ): Salons {
//        اول بررسی میکنیم که کسی که داره اضافه میکنه حتما ادمین باشه
        val user_id = jwt.getIdFromToken(token) ?: throw ExceptionMe("ادمین ناشناخته")
        val admin = userRepository.findById(user_id).orElseThrow { ExceptionMe("ادمین یافت نشد") }
        if (!jwt.validateToken(token, admin)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (admin.role != Role.ADMIN) throw ExceptionMe("اضافه کردن سالن ها فقط از طریق ادمین انجام می شود")
        if (salons.name.isEmpty()) throw ExceptionMe("نام وارد نکرده اید")
        if (salons.password.isEmpty()) throw ExceptionMe("پسورد را وارد نکرده اید")
        if (salons.stakhrorsalon == null) throw ExceptionMe("استخر یا سالن؟؟")
        if (salons.numberPhone.isEmpty()) throw ExceptionMe("شماره سالن را وارد نکرده اید")
        if (salons.address.isEmpty()) throw ExceptionMe("آدرس را وارد نکرده اید")
//        if (salons.activitis.isEmpty()) throw ExceptionMe("فعالیت های سالن را وارد نکرده اید")
        if (activitiesSalonsAndUsers.isEmpty()) throw ExceptionMe("نوع فعالیت ها را مشخص کنید")
        activitiesSalonsAndUsers.forEach { it.salon = salons }
        salons.activitis = activitiesSalonsAndUsers
        val checkDublicate =salonsRepository.existsByNumberPhone(salons.numberPhone)
        if (checkDublicate) throw ExceptionMe("شماره سالن تکراری است قبلا سالن اضافه شده است")
//        پیدا کردن صاحب سالن برای اتصال این دو به هم
        val owner = userRepository.findByNumberPhone(numberPhoneOwner)
            ?: throw ExceptionMe("شماره سالن دار ثبت نشده است اول سالن دار را ثبت نام کنید بعد به ثبت سالن بپردازید")
        if (owner.role != Role.HOLDER) throw ExceptionMe("شخصی که میخواهید به عنوان سالن دار ثبت کنید یک کاربر عادی است")
        salons.owner = owner
        return salonsRepository.save(salons)
    }

    //    ادیت سالن توسط ادمین و سالن دار
    fun update(token: String, salonsId: Long, salons: Salons): Salons {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }

        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        val existingSalon = salonsRepository.findById(salonsId)
            .orElseThrow { ExceptionMe("سالن پیدا نشد") }

        // بررسی دسترسی
        if (user.role != Role.ADMIN && existingSalon.owner?.id != user.id) {
            throw ExceptionMe("شما اجازه آپدیت این سالن را ندارید")
        }
        if (user.role != Role.HOLDER || existingSalon.owner?.id != user.id) {
            throw ExceptionMe("شما اجازه آپدیت این سالن را ندارید")
        }

        if (salons.name.isNotEmpty()) existingSalon.name = salons.name
        if (salons.password.isNotEmpty()) existingSalon.password = salons.password
        if (salons.address.isNotEmpty()) existingSalon.address = salons.address
        if (salons.filmurl.isNotEmpty()) existingSalon.filmurl = salons.filmurl
        if (salons.numberPhone.isNotEmpty()) existingSalon.numberPhone = salons.numberPhone
        existingSalon.betweenWomanMan = salons.betweenWomanMan

        return salonsRepository.save(existingSalon)
    }


    //    گرفتن سالن بر اساس شماره
    fun getSalonsByOwner(token: String): List<Salons> {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        val data = salonsRepository.findSalonsByOwnerId(user.id)
        if (data.isEmpty()) {
            return emptyList()
        }
        return data
    }

    fun getSalonByActivity(activitiesSalonsAndUsersId :Long): List<Salons> {
        val data = salonsRepository.findSalonsByActivitis(activitiesSalonsAndUsersId)
        return data
    }

    //    گرفتن سالن ها برای کاربران عادی
    fun getSalon(pageIndex: Int, pageSize: Int,stakhrOrSalon: StakhrOrSalon): List<Salons> {
        val pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by("id"))
        val data = salonsRepository.findAllByStakhrorsalon(stakhrOrSalon,pageRequest)
        return data.content.toList()
    }
    //    گرفتن سالن خاص برای کاربران عادی
    fun getSalonById(id:Long): Salons {
        val data = salonsRepository.findById(id)
        if (data.isEmpty) throw ExceptionMe("سالن یافت نشد")
        return data.get()
    }
}