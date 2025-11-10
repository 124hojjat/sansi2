package sansino.sansino.controler.reserve

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import sansino.sansino.components.ServiceResponse
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.enums.whreSetImage
import sansino.sansino.repository.reserve.ImageSalonRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.servise.reserve.ImageSalonService

@RestController
@RequestMapping("/api/uploadImage")
class ImageSalonController {

    @Autowired
    private lateinit var imageSalonService: ImageSalonService

    @Autowired
    private lateinit var imageSalonRepository: ImageSalonRepository

    @Autowired
    private lateinit var salonsRepository: SalonsRepository

    //    اپلود عکس برای سالن های ورزشی و ...
    @PostMapping("/upload")
    fun uploadImageForSalon(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam salonId: Long?,
        @RequestParam moarefiSalonId: Long?,
        @RequestParam filedVarzesId: Long?,
        @RequestParam whreSetImage: whreSetImage?,
        @RequestParam description: String?,
        @RequestParam womanAndMan: genderStatus?,
        @RequestParam tablighat: Boolean,
        @RequestParam("file") file: MultipartFile,
    ): ServiceResponse<String> {
        return try {
            // مرحله اول: بررسی اعتبار توکن و احراز هویت
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = imageSalonService.uploadImageSalon(
                token,
                file,
                salonId,
                filedVarzesId,
                tablighat,
                moarefiSalonId,
                whreSetImage,
                womanAndMan = womanAndMan,
                description = description
            )
            ServiceResponse(
                data = listOf(data),
                status = HttpStatus.OK,
                message = "عکس با موفقیت آپلود شد."
            )

        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    @DeleteMapping("/delete")
    fun deleteImage(
        @RequestHeader("Authorization") authHeader: String?,
        @RequestParam imageId: Long,
        @RequestParam salonId: Long?,
        @RequestParam filedVarzesId: Long?,
        @RequestParam moarefiSalonId: Long?,
        @RequestParam tablighat: Boolean
    ): ServiceResponse<Boolean> {
        return try {
            // مرحله اول: بررسی اعتبار توکن و احراز هویت
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
            }
            val token = authHeader.substring(7)
            val data = imageSalonService.deleteImage(
                token,
                imageId,
                salonId,
                filedVarzesId,
                moarefiSalonId,
                tablighat
            )
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


    @GetMapping("/{salonId}")
    fun getImagesBySalon(@PathVariable salonId: Long): ServiceResponse<String> {
        val salon = salonsRepository.findById(salonId)
            .orElseThrow { Exception("Salon not found") }

        val images = imageSalonRepository.findAllBySalon(salon)
        if (images.isEmpty()) {
            return ServiceResponse(
                data = emptyList(),
                status = HttpStatus.OK,
                message = "هیچ عکسی برای این سالن ثبت نشده است."
            )
        }
        val imageUrls = images.map { it.image }
        return ServiceResponse(
            data = imageUrls,
            status = HttpStatus.OK,
            message = "لیست تصاویر با موفقیت دریافت شد."
        )
    }

    @GetMapping("/uploads/{filename}")
    fun getUpload(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable filename: String
    ):ServiceResponse<String>{
        try {
            // مرحله اول: بررسی اعتبار توکن و احراز هویت
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ServiceResponse(data = null, status = HttpStatus.UNAUTHORIZED)
            }
            val token = authHeader.substring(7)
            val data = imageSalonService.getImage(token, filename)
            return ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        }catch (e:Exception){
            return ServiceResponse(data = null, status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


}