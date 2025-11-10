package sansino.sansino.controler.fcm

import org.springframework.web.bind.annotation.*
import sansino.sansino.servise.fcm.FcmService

@RestController
@RequestMapping("/api/fcm")
class FcmController(
    private val fcmService: FcmService
) {
    @PostMapping("/test")
    fun sendTestNotification(@RequestBody req: FcmRequest): String {
        fcmService.sendNotification(req.token, req.title, req.body)
        return "✅ Sent to ${req.token}"
    }

    @PostMapping("/data")
    fun sendTestData(@RequestBody req: FcmDataRequest): String {
        fcmService.sendData(req.token, req.data)
        return "✅ Data sent to ${req.token}"
    }
}

data class FcmRequest(
    val token: String,
    val title: String,
    val body: String
)

data class FcmDataRequest(
    val token: String,
    val data: Map<String, String>
)
