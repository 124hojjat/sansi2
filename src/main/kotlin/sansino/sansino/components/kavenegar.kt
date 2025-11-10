package sansino.sansino.components

import com.kavenegar.sdk.KavenegarApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KavenegarService(
    @Value("\${kavenegar.secretkey}") private val secretKey: String,
    @Value("\${kavenegar.numberphone}") private val numberPhoneSender: String,
) {

    private val api by lazy { KavenegarApi(secretKey) }

    fun send(text: String, phone: String?): String? {
        if (phone.isNullOrBlank()) throw IllegalArgumentException("شماره تلفن معتبر نیست")
        val x = api.verifyLookup(phone, text, "storeAsl")
        return x.message
    }
}
