package sansino.sansino.controler.paymentControler

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.VerifyResult
import sansino.sansino.servise.payment.ZarinPalService

@RestController
@RequestMapping("/api/payment")
class PaymentController(
    private val zarinPalService: ZarinPalService,
) {
    //      ✔
    // 1️⃣ شروع پرداخت
    @GetMapping("/start/{reservationId}")
    fun startPayment(
        @RequestHeader("Authorization") authHeader: String?,
        @PathVariable reservationId: Long,
    ): ServiceResponse<String> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ServiceResponse(status = HttpStatus.UNAUTHORIZED, message = "احراز هویت با خطا روبه رو شده است")
        }
        val token = authHeader.substring(7)
        val paymentUrl = zarinPalService.requestPayment(reservationId, token)
        return ServiceResponse(data = listOf(paymentUrl), status = HttpStatus.OK)
    }

    //      ✔
    // 2️⃣ کال‌بک بعد از پرداخت زرین‌پال
    @GetMapping("/callback")
    fun paymentCallback(
        @RequestParam("Authority") authority: String,
        @RequestParam("Status") status: String
    ): ServiceResponse<VerifyResult> {
        val data = zarinPalService.verifyPayment(authority, status)
        return try {
            ServiceResponse(data = listOf(data), status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(data = null, status = HttpStatus.BAD_REQUEST, message = "${e.message}")
        }
    }
}


/*
*
        // اگر کاربر پرداخت رو لغو کرده باشه:
        if (!status.equals("OK", ignoreCase = true)) {
            transactionService.handlePaymentCallback(authority, false)
            return ServiceResponse(
                data = listOf(false), status = HttpStatus.BAD_REQUEST,
                message = "پرداخت لغو شده است"
            )
        }

        // اگر پرداخت در ظاهر موفق بوده، حالا باید از زرین‌پال استعلام بگیریم
        val transaction = transactionService.findByAuthority(authority)
            ?: return ServiceResponse(
                data = listOf(false),
                status = HttpStatus.BAD_REQUEST,
                message = "تراکنش یافت نشد"
            )

        val verified = zarinPalService.verifyPayment(authority, transaction.amount.toInt())

        if (verified) {
            transactionService.handlePaymentCallback(authority, true)

            return ServiceResponse(
                data = listOf(true),
                status = HttpStatus.OK,
                message = "پرداخت با موفقیت انجام شد ✅"
            )
        } else {
            transactionService.handlePaymentCallback(authority, false)
            return ServiceResponse(
                data = listOf(true),
                status = HttpStatus.BAD_REQUEST, message = "پرداخت توسط زرین پال تأیید نشد"
            )
        }
    }*/