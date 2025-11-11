package sansino.sansino.components

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import sansino.sansino.repository.reserve.UserRepository

@Configuration
@EnableWebSecurity
class SecurityConfigMe {

    companion object {
        // مسیرهایی که بدون JWT آزاد هستند
        val PUBLIC_PATHS = arrayOf(
            "/api/moarefi/getAll",
            "/api/User/registerStep1",
            "/api/User/registerStep2",
            "/api/User/register/user",
            "/api/User/loginStep1",
            "/api/User/loginStep2",
            "/api/tablighat/getAll",
            "/api/salons/getAllSalonOrStakhr",
            "/api/slotTime/getByDateSalons",
            "/api/payment/callback",
            "/api/slotTime/generateNext7Days",
            "/api/uploadImage/uploads/",
            "/api/moarefi/getByField",
            "/api/fcm/",
            "/WS/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/",
            "/webjars/",
            "/api/User/check/Conection"
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtFilter: JwtRequestFilter): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                PUBLIC_PATHS.forEach { path -> auth.requestMatchers("$path**").permitAll() }
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}

@Component
class JwtRequestFilter(
    private val jwtTokenUtils: JwtTokenUtils,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")

            if (authHeader?.startsWith("Bearer ") == true) {
                val token = authHeader.substring(7)
                val userId = jwtTokenUtils.getIdFromToken(token)
                    ?: throw UnauthorizedException("توکن نامعتبر یا فاقد شناسه کاربری")

                val user = userRepository.findById(userId)
                    .orElseThrow { UnauthorizedException("کاربر مرتبط با توکن یافت نشد") }

                val authorities = listOf(SimpleGrantedAuthority(user.role.toString()))
                val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
                SecurityContextHolder.getContext().authentication = auth

            } else if (!isPublicPath(request.servletPath)) {
                throw UnauthorizedException("توکن ارائه نشده است")
            }

            filterChain.doFilter(request, response)

        } catch (ex: UnauthorizedException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.message)
        } catch (ex: Exception) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "خطای سرور")
        }
    }

    private fun isPublicPath(path: String): Boolean {
        return SecurityConfigMe.PUBLIC_PATHS.any { path.startsWith(it) }
    }

    class UnauthorizedException(message: String) : RuntimeException(message)
}
