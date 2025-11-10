package sansino.sansino.controler.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.GetSalonIdDto
import sansino.sansino.components.dto.GetSalonNameDto
import sansino.sansino.components.dto.SalonDto
import sansino.sansino.components.dto.SalonInsertRequest
import sansino.sansino.model.enums.StakhrOrSalon
import sansino.sansino.model.reserve.ActivitiesSalonsAndUsers
import sansino.sansino.model.reserve.Salons
import sansino.sansino.model.reserve.SlotTime
import sansino.sansino.repository.reserve.ImageSalonRepository
import sansino.sansino.servise.reserve.SalonsServise

// jwt به صورت کامل پیاده سازی شد و

@RestController
@RequestMapping("/api/salons")
class SalonsControler {

    @Autowired
    private lateinit var service: SalonsServise
    @Autowired
    private lateinit var imageSalonRepository: ImageSalonRepository


    @PostMapping("/insert")
    fun insert(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestBody request: SalonInsertRequest
    ): ServiceResponse<Salons> {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه‌رو شده است")

        }

        val token = authHeader.removePrefix("Bearer ").trim()

        return try {
            val data = service.insert(
                token,
                request.salons,
                request.numberPhoneOwner,
                request.activitiesSalonsAndUsers
            )
            data.password = ""
            data.numberPhone = ""
            data.owner?.password = ""
            data.owner?.numberPhone = ""
            ServiceResponse(
                data = listOf(
                    data
                ), status = HttpStatus.OK
            )
        } catch (e: ExceptionMe) {
            ServiceResponse(status = HttpStatus.BAD_REQUEST, message = "${e.message}")
        } catch (e: Exception) {
            ServiceResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "${e.message}"
            )

        }
    }


    @PutMapping("/update")
    fun update(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam salonsId: Long,
        @RequestBody salons: Salons,
    ): ServiceResponse<SalonDto> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.update(token, salonsId, salons)
            val galleryImages = imageSalonRepository.findAllBySalonId(data.id)?.map { it.image }

            return ServiceResponse(
                data = listOf(
                    SalonDto(
                        id = data.id,
                        name = data.name,
                        numberPhone = data.numberPhone,
                        address = data.address,
                        filmurl = data.filmurl,
                        betweenWomanMan = data.betweenWomanMan,
                        activitis = data.activitis,
                        galleryImages = galleryImages,
                        owner = data.owner?.numberPhone
                    )
                ), status = HttpStatus.OK
            )
        } catch (e: Exception) {
            return ServiceResponse(
                data = null,
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                message = "${e.message}"
            )
        }

    }

    @GetMapping("/getByOwner")
    fun getByOwner(
        @RequestHeader("Authorization") authHeader: String?,
    ): ServiceResponse<Salons> {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = service.getSalonsByOwner(token = token)
            data.forEach { item ->
                item.password = ""
                item.slotTimes = mutableListOf()
                item.owner?.reservation = null
                item.owner?.password = ""
            }
            return ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, message = "${e.message}", status = HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    //    یوزر میخواد لیست سالن ها رو ببینه
    @GetMapping("/getAllSalonOrStakhr")
    fun getAll(
        @RequestParam pageIndex: Int,
        @RequestParam pageSize: Int,
        @RequestParam stakhrOrSalon: StakhrOrSalon
    ): ServiceResponse<GetSalonNameDto> {
        try {
            val data = service.getSalon(pageIndex, pageSize, stakhrOrSalon)
            data.forEach { item -> item.password = "" }
            val maping = data.map { item ->
                GetSalonNameDto(
                    id = item.id,
                    name = item.name,
                    address = item.address,
                    imageUrl = item.imageurls
                )
            }
            return ServiceResponse(data = maping, status = HttpStatus.OK)
        } catch (e: Exception) {
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, "${e.message}")
        }
    }


    //todo : اینجا درز اطلاعات داریم مقدار برگشتی پسورد رو برمیگردونه
    //    ✔
    @GetMapping("/getAllByActivity")
    fun getAllByActivity(
        @RequestParam activitiesSalonsAndUsersId: Long
    ): ServiceResponse<Salons> {
        return try {
            val data = service.getSalonByActivity(activitiesSalonsAndUsersId)
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }

    @GetMapping("/getSalonById")
    fun getSalonById(@RequestParam id: Long): ServiceResponse<GetSalonIdDto> {
        return try {
            val data = service.getSalonById(id)
            val dto = GetSalonIdDto(
                id = data.id,
                name = data.name,
                address = data.address,
                filmUrl = data.filmurl,
                betweenWomanMan = data.betweenWomanMan,
//                slotTimes = data.slotTimes,
                imageurls = data.imageurls,
                activitis = data.activitis
            )
            ServiceResponse(data = listOf(dto), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }
}