package sansino.sansino

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
// ب@EnableJpaAuditing برای اینه که میخواییم توی گزارش ها به صورت خودکار زمان اپدیت بشه
@EnableJpaAuditing
@EnableScheduling
class SansinoApplication

fun main(args: Array<String>) {
	runApplication<SansinoApplication>(*args)
}
