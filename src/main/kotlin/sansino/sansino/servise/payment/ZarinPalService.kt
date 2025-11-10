package sansino.sansino.servise.payment

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import sansino.sansino.components.ExceptionMe
import sansino.sansino.components.JwtTokenUtils
import sansino.sansino.components.ServiceResponse
import sansino.sansino.components.dto.VerifyResult
import sansino.sansino.model.enums.ReservationStatue
import sansino.sansino.repository.report.ReportsRepository
import sansino.sansino.repository.reserve.ReservationRepository
import sansino.sansino.repository.reserve.UserRepository
import sansino.sansino.servise.reserve.TransActionService

@Service
class ZarinPalService {

    @Autowired
    private lateinit var reportsRepository: ReportsRepository

    //    todo :اینا باید بره یه جای امن نه اینجا
    private val merchantId = "1277ca74-2f5b-4b0d-895f-174cd3238cfe"
    private val callbackUrl = "https://nazaninyekta.ir/payment/callback"


    @Autowired
    private lateinit var transActionService: TransActionService

    @Autowired
    private lateinit var jwt: JwtTokenUtils

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var reservationRepository: ReservationRepository


    //    استفاده در کنترلر
    fun requestPayment(reservationId: Long, token: String): String {
//        چک کردن توکن
        val userId = jwt.getIdFromToken(token) ?: throw ExceptionMe("کاربر ناشناخته")
        val user = userRepository.findById(userId)
            .orElseThrow { ExceptionMe("کاربر یافت نشد") }
        if (!jwt.validateToken(token, user)) throw ExceptionMe("توکن نامعتبر یا منقضی شده")
//        پیدا کردن رزروی که قراره پرداخت بشه
        val reserve = reservationRepository.findById(reservationId)
        if (reserve.get().status == ReservationStatue.CONFIRMED) return "قبلا این رزرو پرداخت شده است"
        if (reserve.get().status == ReservationStatue.CANCELLED) return "این رزرو کنسل شده است دوباره رزرو کنید"
        val amount = reserve.get().amount
//        چک کردن اینکه توکن و رزرو به هم مربوط باشه
        if (user.id != reserve.get().user.id) throw ExceptionMe("تراکنش برای شما نیست")
        // 1. ایجاد تراکنش در DB
        val transaction = transActionService.initiatePayment(reservationId, amount.toString())
        // 2. ارسال درخواست به زرین‌پال
        val client = RestTemplate()
        val request = mapOf(
            "merchant_id" to merchantId,
            "amount" to amount,
            "callback_url" to callbackUrl,
            "description" to "پرداخت رزرو شماره ${reservationId}",
            "email" to "",
            "mobile" to ""
        )

        val response = client.postForObject(
            "https://sandbox.zarinpal.com/pg/v4/payment/request.json",
            request,
            Map::class.java
        )
        val data = response?.get("data") as? Map<*, *>
            ?: throw IllegalStateException("Missing data in Zarinpal response")

        val code = data["code"] as? Int ?: throw IllegalStateException("No code in Zarinpal response")
        if (code != 100) {
            throw IllegalStateException("Payment request failed: ${data["message"]}")
        }

        val authority = data["authority"] as? String
            ?: throw IllegalStateException("Authority not received from Zarinpal")

        // ذخیره authority در ترنس اکشن
        transActionService.updateTransactionRef(transaction.id, authority)

        // 3. برگرداندن URL پرداخت برای هدایت کاربر
        return "https://sandbox.zarinpal.com/pg/StartPay/$authority"
    }

    fun verifyPayment(authority: String, status: String): VerifyResult {
        val transaction = transActionService.findByAuthority(authority)
            ?: throw ExceptionMe("تراکنش یافت نشد")

        if (!status.equals("OK", ignoreCase = true)) {
            transActionService.handlePaymentCallback(authority, false)
            return VerifyResult(false, "پرداخت لغو شده است", transaction.id)
        }

        val client = RestTemplate()
        val request = mapOf(
            "merchant_id" to merchantId,
            "amount" to transaction.amount.toInt(),
            "authority" to authority
        )

        val response = client.postForObject(
            "https://sandbox.zarinpal.com/pg/v4/payment/verify.json",
            request,
            Map::class.java
        )

        val data = response?.get("data") as? Map<*, *>
        val code = data?.get("code") as? Int
        val refId = data?.get("ref_id")?.toString()

        return if (code == 100) {
            transActionService.handlePaymentCallback(authority, true)
            VerifyResult(true, "پرداخت با موفقیت انجام شد ✅", transaction.id, refId)
        } else {
            transActionService.handlePaymentCallback(authority, false)
            VerifyResult(false, "پرداخت توسط زرین پال تأیید نشد", transaction.id)
        }
    }


}


/*
*
    fun verifyPayment(authority: String, amount: Int): Boolean {
        val client = RestTemplate()
        val request = mapOf(
            "merchant_id" to merchantId,
            "amount" to amount,
            "authority" to authority
        )

        val response = client.postForObject(
            "https://sandbox.zarinpal.com/pg/v4/payment/verify.json",
            request,
            Map::class.java
        )

        val data = response?.get("data") as? Map<*, *>
        val code = data?.get("code") as? Int
        return code == 100
    }
*/