package sansino.sansino.controler.moarefi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ServiceResponse
import sansino.sansino.model.moarefi.filedVarzes
import sansino.sansino.servise.moarefi.FiledVarzesService


@RestController
@RequestMapping("/api/fieldVarzesh")
class FildeVarzesControler {
    @Autowired private lateinit var service: FiledVarzesService


    @PostMapping("/insert")
    fun insert(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody filedVarzes: filedVarzes
    ): ServiceResponse<filedVarzes> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "توکن اشتباه")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.insert(token, filedVarzes)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @DeleteMapping("/delete")
    fun delete(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam filedVarzesId: Long
    ): ServiceResponse<Boolean> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED, message = "توکن اشتباه")
        }
        val token = authHeader.substring(7)
        return try {
            val data = service.delete(token, filedVarzesId)
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/getAll")
    fun getAll():ServiceResponse<filedVarzes>{
        return try {
            val data = service.getAll()
            ServiceResponse(data = data, status = HttpStatus.OK)
        }catch (e:Exception){
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }
}