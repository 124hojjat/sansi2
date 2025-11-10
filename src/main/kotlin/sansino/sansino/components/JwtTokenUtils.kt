package sansino.sansino.components


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.io.Decoders
import javax.crypto.SecretKey
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sansino.sansino.model.reserve.User
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Function
import javax.crypto.spec.SecretKeySpec


@Component
class JwtTokenUtils() : Serializable {


    val JWT_TOKEN_VALIDITY = (5 * 60 * 60).toLong()

    @Value("\${jwt.secret}")
    lateinit var secret: String

    //retrieve username from jwt token
    fun getIdFromToken(token: String): Long? {
        val data = getClaimFromToken(token, Function { it.subject })
        return data.toLongOrNull()
    }

    private fun getSigningKey(): SecretKeySpec {
        val secretBytes = secret.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(secretBytes, SignatureAlgorithm.HS512.jcaName)
    }

    //retrieve expiration date from jwt token
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Function { it.expiration })
    }

    fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }

    fun getAllClaimsFromToken(token: String): Claims {
        val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
        return try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: Exception) {
            throw IllegalArgumentException("توکن نامعتبر است: ${e.message}")
        }
    }


    private fun getSignInKey(): SecretKey {
        // فرض Base64-encoded secret
        val keyBytes = Decoders.BASE64.decode(secret)
        return Keys.hmacShaKeyFor(keyBytes)
    }


    //check if the token has expired
    private fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationDateFromToken(token)
        return expiration.before(Date())
    }

    //generate token for user
    fun generateToken(userDetails: Long): String? {
        val claims: Map<String, Any> = HashMap()
        return doGenerateToken(claims, userDetails.toString())
    }

    // تولید توکن با جزئیات و امضای کلید
    private fun doGenerateToken(claims: Map<String, Any>, subject: String): String {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(getSignInKey(), Jwts.SIG.HS512)
            .compact()
    }


    //validate token
    fun validateToken(token: String, userDetails: User): Boolean {
        val idFromToken = getIdFromToken(token)
        return idFromToken == userDetails.id && !isTokenExpired(token)
    }
}
