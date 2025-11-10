package sansino.sansino.components

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class swagerConfig {
    @Bean
    fun api(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("api")
            .pathsToMatch("/api/**")
            .packagesToScan("sansino.sansino.controler")
            .build()
    }

}