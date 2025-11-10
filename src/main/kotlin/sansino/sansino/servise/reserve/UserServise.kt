package sansino.sansino.servise.reserve

import com.kavenegar.sdk.KavenegarApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.components.KavenegarService
//import sansino.sansino.components.kavenegar
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.pendingUser.PendingUser
import sansino.sansino.model.reserve.User
import sansino.sansino.repository.pendingUser.PendingUserRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import java.time.LocalDateTime


@Service
class UserServise(private var kavenegar: KavenegarService) {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var pendingUser: PendingUserRepository

    @Autowired
    private lateinit var jwtTokenUtils: JwtTokenUtils


    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var salonRepository: SalonsRepository
    //    register ثبت نام
//    اطلاعات رو میگیریم و میریزیم داخل دیتابیس pending user و پیامک میفرستیم به شمارش
    fun registerStep1(user: User): Boolean {
        if (user.name.isEmpty()) throw ExceptionMe("لطفا نام خود را وارد کنید")
        if (user.gender.name.isEmpty()) throw ExceptionMe("لطفا جنسیت خود را مشخص کنید")
        if (user.numberPhone.isEmpty()) throw ExceptionMe("لطفا شماره خود را وارد کنید")
        val checkNumber = userRepository.findByNumberPhone(user.numberPhone)
        if (checkNumber != null) throw ExceptionMe("این شماره قبلا ثبت نام کرده است")
        if (user.role == null) throw ExceptionMe("نقش مشخص نیست")
//        سالن دار و ادمین باید رمز ست کنن ولی کاربر نمیخواد
        if (user.role == Role.ADMIN || user.role == Role.HOLDER) {
            if (user.password.isNullOrEmpty()) throw ExceptionMe("ادمین برنامه و سالن دار باید رمز داشته باشند")
        }
        //        یه عدد رندوم بساز
        val otp = (100000..999999).random().toString()
        //        زمان روی دو دقیقه ست میشه
        val expiresAt = LocalDateTime.now().plusMinutes(2)
//        فرستادن کد تایید به سرور
        println("otp : $otp")
//       val send = kavenegar.send(text = otp, user.numberPhone)
        pendingUser.save(
            PendingUser(
                name = user.name,
                gender = user.gender,
                phoneNumber = user.numberPhone,
                role = user.role!!,
                otpCode = otp,
                expiresAt = expiresAt,
                password = passwordEncoder.encode(user.password ?: "")
            )
        )

        return true
    }
    @Transactional
    fun registerStep2(phone: String, code: String): String {
        if (phone.isEmpty()) throw ExceptionMe("شماره تلفن وارد نشده است")
        if (code.isEmpty()) throw ExceptionMe("کد تائید را وارد نکرده اید")
//        برو بر اساس شماره تلفن و کد ببین چنین چیزی هست یا نه
        val user = pendingUser.findByPhoneNumberAndOtpCode(phone, code)
//        زمان کد تایید نگذشته باشه
        if (user == null || user.expiresAt?.isBefore(LocalDateTime.now())!!) {
            throw ExceptionMe("کد تائید نادرست یا منقضی شده است ")
        } else {
            val savedUser = User(
                name = user.name,
                gender = user.gender,
                numberPhone = user.phoneNumber,
                role = user.role,
                password =user.password,
            )

            userRepository.save(savedUser)
            pendingUser.delete(user)
            val token =
                jwtTokenUtils.generateToken(savedUser.id) ?: throw ExceptionMe("دوباره وارد شوید مشکل در احراز هویت")
            return token
        }
    }
    //    ورود به برنامه log in
    fun loginStep1(phone: String, password: String?): Boolean {
        if (phone.isEmpty()) throw ExceptionMe("شماره تلفن وارد نشده است")
        val user = userRepository.findByNumberPhone(phone) ?: throw ExceptionMe("قبلا ثبت نام نکرده اید")


        if (user.role != Role.KARBAR) {
            if (password.isNullOrEmpty()) throw ExceptionMe("رمز وارد نشده است")
            if (!passwordEncoder.matches(password,user.password)) throw ExceptionMe("رمز اشتباه است")
        }

        //        یه عدد رندوم بساز
        val otp = (100000..999999).random().toString()
        //        زمان روی دو دقیقه ست میشه
        val expiresAt = LocalDateTime.now().plusMinutes(2)
//        فرستادن کد تایید به سرور
        println("otp $otp")
//        todo :برای تست روی رندر هست حتما باید درستش کنی
//        kavenegar.send(
//            text = "سلام به اپلیکیشن خوش آمدید کد ورود شما : مدت اعتبار این کد 2 دقیقه است $otp",
//            phone = phone
//        )
        pendingUser.save(
            PendingUser(
                name = "در حال عملیات ورود",
                gender = genderStatus.MAN,
                phoneNumber = phone,
                role = Role.KARBAR,
                otpCode = otp,
                expiresAt = expiresAt,
                password = ""
            )
        )
        return true

    }
    @Transactional
    fun loginStep2(phone: String, code: String): String {
        if (phone.isEmpty()) throw ExceptionMe("شماره تلفن وارد نشده است")
        if (code.isEmpty()) throw ExceptionMe("کد تائید را وارد نکرده اید")
        val data = userRepository.findByNumberPhone(phone) ?: throw ExceptionMe("قبلا ثبت نام نکرده اید")

        //        برو بر اساس شماره تلفن و کد ببین چنین چیزی داخل  pending هست یا نه
        val user = pendingUser.findByPhoneNumberAndOtpCode(phone, code)
//        زمان کد تایید نگذشته باشه
        if (user?.expiresAt == null || user.expiresAt.isBefore(LocalDateTime.now())) {
            throw ExceptionMe("کد تائید نادرست یا منقضی شده است ")
        } else {
            val token = jwtTokenUtils.generateToken(data.id)
            userRepository.save(data)
            pendingUser.delete(user)
            if (token == null) throw ExceptionMe("دوباره وارد شوید مشکل در احراز هویت")
            return token
        }


    }
    fun changePassword(token: String, oldPassword: String, newPassword: String): Boolean {
        if (oldPassword.isEmpty()) throw ExceptionMe("رمز قبلی را وارد کنید")
        if (newPassword.isEmpty()) throw ExceptionMe("رمز جدید نمی تواند خالی باشد")
        if (newPassword.length < 8) throw ExceptionMe("حداقل 8 کاراکتر باید داشته باشد")
        if (oldPassword == newPassword)
            throw ExceptionMe("رمز جدید نمی‌تواند با رمز فعلی یکسان باشد")

        //        وضعیت توکن
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
//        وضعیت کد
        if (!passwordEncoder
                .matches(oldPassword, user.password)
        ) throw ExceptionMe("رمز قبلی اشتباه است")

        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        return true
    }
    fun resetPassword(token: String,salonId :Long,numberPhoneOwner:String):String {
        if (numberPhoneOwner.isEmpty()) throw ExceptionMe("شماره صاحب مجموعه را وارد کنید")
        if (salonId.toString().isEmpty()) throw ExceptionMe(" سالن را انتخاب نکرده اید")
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user.role != Role.ADMIN) throw ExceptionMe("ادمین نیستید ")
        val salon = salonRepository.findById(salonId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (salon.owner?.numberPhone != numberPhoneOwner) throw ExceptionMe("شماره مربوط به صاحب مجموعه نیست")
        //        فرستادن کد تایید به سرور

        val otp = (100000..999999).random().toString()
        kavenegar.send(
            text = "رمز جدید : $otp",
            salon.owner?.numberPhone
        )
        salon.password = passwordEncoder.encode(otp)
        salonRepository.save(salon)
        return "رمز به شماره سالن دار پیامک شد"

    }

    fun getUser(token: String?): User {
//        چک کردن توکن
        if (token ==null)throw ExceptionMe("دوباره وارد شوید")
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        if (user == null) throw ExceptionMe("کاربر یافت نشد")
        return user
    }
}