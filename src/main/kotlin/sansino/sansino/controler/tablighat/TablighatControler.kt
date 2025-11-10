package sansino.sansino.controler.tablighat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sansino.sansino.components.ServiceResponse
import sansino.sansino.model.tablighat.Tablighat
import sansino.sansino.repository.tablighat.TablighatRepository

@RestController
@RequestMapping("/api/tablighat")
class TablighatControler {

    @Autowired
    private lateinit var tablighatRepository: TablighatRepository


//    گرفتن تبلیغات
    @GetMapping("/getAll")
    fun getAllTables(): ServiceResponse<Tablighat> {
        return try {
            val data = tablighatRepository.findAll()
            ServiceResponse(data = data, status = HttpStatus.OK)
        } catch (e: Exception) {
            ServiceResponse(status = HttpStatus.INTERNAL_SERVER_ERROR, message = "${e.message}")
        }
    }


}