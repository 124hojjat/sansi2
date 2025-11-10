package sansino.sansino.controler.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.*
import sansino.sansino.model.enums.Role
import sansino.sansino.model.reserve.User
import sansino.sansino.repository.reserve.UserRepository
import sansino.sansino.servise.reserve.UserServise


@RestController
@RequestMapping("/api/User")
class UserControler() {

    @Autowired
    private lateinit var servise: UserServise

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository


    @PostMapping("/register/user")
    fun registerUserStep1(@RequestBody req: RegisterRequest): ServiceResponse<Boolean> {
        try {
            val data = servise.registerStep1(
                User(
                    name = req.name,
                    role = Role.KARBAR,
                    gender = req.genderStatus,
                    numberPhone = req.phone
                )
            )
            return ServiceResponse(data = listOf(data), HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @PostMapping("/register/salon")
    fun registerSalonStep1(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody req: RegisterRequest
    ): ServiceResponse<Boolean> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
            val user = userRepository.findById(userId)
                .orElseThrow { ExceptionMe("کاربر یافت نشد") }
            if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
            if (user.role != Role.ADMIN) throw ExceptionMe("ادمین نیستید ")
            val data = servise.registerStep1(
                User(
                    name = req.name,
                    password = req.password,
                    role = Role.HOLDER,
                    gender = req.genderStatus,
                    numberPhone = req.phone
                )
            )
            return ServiceResponse(data = listOf(data), HttpStatus.OK)

        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @PostMapping("/register/admin")
    fun registerAdmin(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody req: RegisterRequest
    ): ServiceResponse<Boolean> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
            val user = userRepository.findById(userId)
                .orElseThrow { ExceptionMe("کاربر یافت نشد") }
            if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
            if (user.role != Role.ADMIN) throw ExceptionMe("ادمین نیستید ")
            val data = servise.registerStep1(
                User(
                    name = req.name,
                    password = req.password,
                    role = Role.ADMIN,
                    gender = req.genderStatus,
                    numberPhone = req.phone
                )
            )
            return ServiceResponse(data = listOf(data), HttpStatus.OK)

        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    //      ✔
    @PostMapping("/registerStep2")
    fun registerStep2(
        @RequestBody req: Login2Dto
    ): ServiceResponse<String> {
        try {
            val data = servise.registerStep2(req.phone, req.code)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    //      ✔
    @PostMapping("/loginStep1")
    fun login(
        @RequestBody req: Login1Dto
    ): ServiceResponse<Boolean> {
        try {
            val data = servise.loginStep1(req.phone, req.password)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, "${e.message}")
        }
    }

    //      ✔
    @PostMapping("/loginStep2")
    fun login2(
        @RequestBody req: Login2Dto
    ): ServiceResponse<String> {
        try {
            val data = servise.loginStep2(req.phone, req.code)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, "${e.message}")
        }
    }


    @PutMapping("/changePassword")
    fun changePassword(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody request: ChangePasswordRequest
    ): ServiceResponse<Boolean> {
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "توکن ارسال نشده یا نامعتبر است")

        return try {
            val token = authHeader.substring(7)
            val result = servise.changePassword(token, request.oldPassword, request.newPassword)
            ServiceResponse(data = listOf(result), status = HttpStatus.OK)
        } catch (e: ExceptionMe) {
            ServiceResponse(status = HttpStatus.BAD_REQUEST, message = "${e.message}")
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "خطای غیرمنتظره سرور")
        }
    }


    @PutMapping("/resetPassword")
    fun resetPassword(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam salonId: Long,
        @RequestParam numberPhoneOwner: String
    ): ServiceResponse<String> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = servise.resetPassword(token, salonId, numberPhoneOwner)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: ExceptionMe) {
            ServiceResponse(data = null, status = HttpStatus.BAD_REQUEST, message = "${e.message}")
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "خطای غیر منتظره")
        }
    }


    @GetMapping("/getUser")
    fun getUser(
        @RequestHeader("Authorization") authHeader: String?,
    ): ServiceResponse<GetUserDto> {
        return try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = servise.getUser(token = token)
            val dto = GetUserDto(
                name = data.name,
                numberPhone = data.numberPhone,
                genderStatus = data.gender,
            )
            ServiceResponse(data = listOf(dto), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.BAD_REQUEST, message = "${e.message}")
        }
    }


    @GetMapping("/check/Conection")
    fun checkConection(): ServiceResponse<Boolean> {
        return ServiceResponse(data = listOf(true), status = HttpStatus.OK)
    }
}