package sansino.sansino.servise.reserve

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.model.enums.Role
import sansino.sansino.model.enums.genderStatus
import sansino.sansino.model.enums.whreSetImage
import sansino.sansino.model.reserve.ImagesSalon
import sansino.sansino.model.reserve.User
import sansino.sansino.model.tablighat.Tablighat
import sansino.sansino.repository.moarefi.FiledVarzesRepository
import sansino.sansino.repository.moarefi.MoarefiSalonsRepository
import sansino.sansino.repository.reserve.ImageSalonRepository
import sansino.sansino.repository.reserve.SalonsRepository
import sansino.sansino.repository.reserve.UserRepository
import sansino.sansino.repository.tablighat.TablighatRepository
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.*

@Service
class ImageSalonService {

    @Value("\${arvancloud.access.key}") private lateinit var accessKey: String
    @Value("\${arvancloud.secret.key}") private lateinit var secretKey: String
    @Value("\${arvancloud.bucket.name}") private lateinit var bucketName: String
    @Value("\${arvancloud.endpoint}") private lateinit var endpoint: String
    @Autowired private lateinit var moarefiSalonsRepository: MoarefiSalonsRepository
    @Autowired private lateinit var jwt: JwtTokenUtils
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var salonsRepository: SalonsRepository
    @Autowired private lateinit var imageSalonRepository: ImageSalonRepository
    @Autowired private lateinit var tablighatRepository: TablighatRepository
    @Autowired private lateinit var fildVarzesRepository: FiledVarzesRepository

    private lateinit var s3Client: S3Client

    @PostConstruct
    fun initS3() {
        s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.of("ir-thr-at1"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build()
    }



    //    اپلود عکس برای سالن ها
    fun uploadImageSalon(
        token: String,
        file: MultipartFile,
        salonId: Long?,
        fildVarzesId: Long?,
        tablighat: Boolean = false,
        moarefiSalonId: Long?,
        whreSetImage: whreSetImage?,
        womanAndMan: genderStatus?,
        description: String?,
    ): String {

        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
        // مرحله دوم: بررسی اعتبار فایل
        if (file.isEmpty) throw ExceptionMe("فایلی ارسال نشده است.")
        val contentType = file.contentType ?: ""
        val extension = file.originalFilename?.substringAfterLast(".")?.toLowerCase()
        val allowedImageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp")
        val allowedVideoExtensions = listOf("mp4", "avi", "mov", "mkv")
        val isImage = (contentType.startsWith("image/") || (extension != null && extension in allowedImageExtensions))
        val isVideo = (contentType.startsWith("video/") || (extension != null && extension in allowedVideoExtensions))
        if (!isImage && !isVideo) throw ExceptionMe("فقط تصاویر و ویدیوها مجاز هستند.")

        if (salonId != null && fildVarzesId == null && moarefiSalonId == null && !tablighat) {
            // مرحله سوم: یافتن سالن
            val salon = salonsRepository.findById(salonId).orElseThrow { ExceptionMe("Salon not found") }
            if (user.role != Role.ADMIN && salon.owner != user)
                throw ExceptionMe("شما اجازه‌ی افزودن عکس برای این سالن را ندارید")

            // مرحله چهارم: ذخیره‌سازی فایل (اگر همه شرایط بالا برقرار باشند)
            val fileUrl = try {
                setName(extension, file)
            } catch (ex: IOException) {
                throw ExceptionMe("خطا در ذخیره‌سازی فایل.")
            }

            imageSalonRepository.save(ImagesSalon(image = fileUrl, salon = salon))
            return fileUrl
        } else if (salonId == null && fildVarzesId != null && moarefiSalonId == null && !tablighat) {
            // مرحله سوم: یافتن فیلد ورزشی
            val fildVarzes = fildVarzesRepository.findById(fildVarzesId)
                .orElseThrow { ExceptionMe("fildVarzesh not found") }
            // مرحله چهارم: ذخیره‌سازی فایل (اگر همه شرایط بالا برقرار باشند)
            val fileUrl = try {
                setName(extension, file)
            } catch (ex: IOException) {
                throw ExceptionMe("خطا در ذخیره‌سازی فایل.")
            }
            imageSalonRepository.save(ImagesSalon(image = fileUrl))
            fildVarzes.imageSalon = fileUrl
            fildVarzesRepository.save(fildVarzes)
            return fileUrl
        } else if (salonId == null && fildVarzesId == null && moarefiSalonId != null && !tablighat) {
            // مرحله سوم: یافتن معرفی سالن ها
            val moarefiSalons = moarefiSalonsRepository.findById(moarefiSalonId)
                .orElseThrow { ExceptionMe("سالن پیدا نشد") }
            // مرحله چهارم: ذخیره‌سازی فایل (اگر همه شرایط بالا برقرار باشند)
            val fileUrl = try {
                setName(extension, file)
            } catch (ex: IOException) {
                throw ExceptionMe("خطا در ذخیره‌سازی فایل.")
            }
            val imageSalon = imageSalonRepository.save(
                ImagesSalon(
                    image = fileUrl,
                    whereSet = whreSetImage,
                    description = description,
                    womanAndMan = womanAndMan,
                    moarefiSalon_Id = moarefiSalons
                )
            )
            moarefiSalons.imageurls.add(imageSalon)
            moarefiSalonsRepository.save(moarefiSalons)
            return fileUrl
        } else if (salonId == null && fildVarzesId == null && moarefiSalonId == null && tablighat) {
            // مرحله سوم: یافتن معرفی سالن ها
            // مرحله چهارم: ذخیره‌سازی فایل (اگر همه شرایط بالا برقرار باشند)
            val fileUrl = try {
                setName(extension, file)
            } catch (ex: IOException) {
                throw ExceptionMe("خطا در ذخیره‌سازی فایل.")
            }
            imageSalonRepository.save(
                ImagesSalon(
                    image = fileUrl,
                    whereSet = whreSetImage,
                    description = description,
                    womanAndMan = womanAndMan,
                    tablighat = true
                )
            )
            tablighatRepository.save(Tablighat(imageUrl = fileUrl))
            return fileUrl
        } else {
            throw ExceptionMe("خطا دوباره تلاش کنید")
        }
    }

    fun setName(extension: String?, multipartFile: MultipartFile): String {
        val finalExtension = extension ?: "jpg"
        val safeName = "${UUID.randomUUID()}.$finalExtension"
        // ساخت فایل موقت
        val tempFile = File.createTempFile("upload-", safeName)
        multipartFile.transferTo(tempFile)
        // آپلود به باکت
        val request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(safeName)
            .acl("public-read")
            .build()
        s3Client.putObject(request, RequestBody.fromFile(tempFile))

        // حذف فایل موقت
        tempFile.delete()

        // لینک نهایی
        return "https://$bucketName.s3.ir-thr-at1.arvanstorage.com/$safeName"
    }

    @Transactional
    fun deleteImage(
        token: String,
        imageId: Long,
        salonId: Long? = null,
        fildVarzesId: Long? = null,
        moarefiSalonId: Long? = null,
        tablighat: Boolean = false,
    ): Boolean {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        // 🟢 حذف عکس فیلد ورزشی
        fildVarzesId?.let {
            ensureAdmin(user)
            val field = fildVarzesRepository.findById(it)
                .orElseThrow { ExceptionMe("فیلد ورزشی پیدا نشد") }

            field.imageSalon?.let { deleteFileFromDisk(it) }
            field.imageSalon = null
            fildVarzesRepository.save(field)
            return true
        }

        // 🟢 حذف عکس معرفی سالن
        moarefiSalonId?.let {
            ensureAdmin(user)
            val moarefiSalon = moarefiSalonsRepository.findById(it)
                .orElseThrow { ExceptionMe("سالن پیدا نشد") }

            moarefiSalon.imageurls.forEach { img ->
                deleteFileFromDisk(img.image)
                imageSalonRepository.delete(img)
            }
            moarefiSalon.imageurls.clear()
            moarefiSalonsRepository.save(moarefiSalon)
            return true
        }

        // 🟢 حذف عکس‌های سالن
        salonId?.let {
            ensureAdminOrHolder(user)
            val salon = salonsRepository.findById(it)
                .orElseThrow { ExceptionMe("سالن پیدا نشد") }

            if (user.role == Role.HOLDER && salon.owner?.id != user.id)
                throw ExceptionMe("شما اجازه حذف تصاویر سالن دیگران را ندارید")

            val images = imageSalonRepository.findAllBySalonId(salon.id)
            images?.forEach { img ->
                deleteFileFromDisk(img.image)
                imageSalonRepository.delete(img)
            }
            return true
        }

        // 🟢 حذف تبلیغات
        if (tablighat) {
            ensureAdmin(user)
            val image = imageSalonRepository.findById(imageId)
                .orElseThrow { ExceptionMe("تصویر یافت نشد") }

            deleteFileFromDisk(image.image)
            imageSalonRepository.delete(image)
            return true
        }

        throw ExceptionMe("هیچ شناسه‌ای برای حذف داده نشده است")
    }

    private fun ensureAdmin(user: User) {
        if (user.role != Role.ADMIN)
            throw ExceptionMe("فقط ادمین مجاز است")
    }

    private fun ensureAdminOrHolder(user: User) {
        if (user.role != Role.ADMIN && user.role != Role.HOLDER)
            throw ExceptionMe("شما اجازه انجام این عملیات را ندارید")
    }

    fun deleteFileFromDisk(fileUrl: String):String {
         try {
            val fileName = fileUrl.substringAfterLast("/")
            s3Client.deleteObject { it.bucket(bucketName).key(fileName) }
             return "فایل حذف شد"
        } catch (ex: Exception) {
            return "خطا در حذف فایل"
        }
    }


    fun getImage(token: String, filename: String): String {
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId).orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")

        return "https://$bucketName.s3.ir-thr-at1.arvanstorage.com/$filename"
    }





}