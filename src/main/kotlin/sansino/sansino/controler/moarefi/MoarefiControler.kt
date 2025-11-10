package sansino.sansino.controler.moarefi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.MoarefiControlerDtoSend
import sansino.sansino.model.moarefi.MoarefiSalons
import sansino.sansino.servise.moarefi.MoarefiSalonsServise

@RestController
@RequestMapping("/api/moarefi")
class MoarefiControler {

    @Autowired
    private lateinit var service: MoarefiSalonsServise

    @PostMapping("/insert")
    fun insert(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody moarefiControlerDtoSend: MoarefiControlerDtoSend,
    ): ServiceResponse<MoarefiSalons> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
            val data =
                service.insert(token, moarefiControlerDtoSend.moarefiSalons, moarefiControlerDtoSend.filedVarzesId)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @PutMapping("/update")
    fun update(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody moarefiControlerDtoSend: MoarefiControlerDtoSend,
    ): ServiceResponse<MoarefiSalons> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
            val data =
                service.update(token, moarefiControlerDtoSend.moarefiSalons, moarefiControlerDtoSend.filedVarzesId)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @DeleteMapping("/delete")
    fun delete(
        @RequestBody moarefiSalons: MoarefiSalons,
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam("password") password: String
    ): ServiceResponse<Boolean> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.delete(token, moarefiSalons)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/getNumber")
    fun getByNumber(
        @RequestBody numberPhone: String,
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam("password") password: String
    ): ServiceResponse<MoarefiSalons> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.getByNumberPhone(token, numberPhone)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/getAll")
    fun getAll(
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int
    ): ServiceResponse<MoarefiSalons> {
        return try {
            val data = service.getAll(pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    //      ✔
    @GetMapping("/getById")
    fun getById(
        @RequestParam salonId: Long
    ): ServiceResponse<MoarefiSalons> {
        return try {
            val data = service.getById(salonId)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    //      ✔
    @GetMapping("/getByField")
    fun getByField(
        @RequestParam filedVarsesId: Long,
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int
    ): ServiceResponse<MoarefiSalons> {
        return try {
            val data = service.getAllByField(filedVarsesId, pageIndex, pageSize)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }
}