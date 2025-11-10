package sansino.sansino.components

import org.springframework.http.HttpStatus
import java.io.Serializable

class ServiceResponse<T>(
    val data :List<T>? = null,
    val status : HttpStatus,
    val message:String = ""
): Serializable {
}