package bg.prices.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableScheduling
@EnableCaching
class BgPricesBackendApplication

fun main(args: Array<String>) {
    runApplication<BgPricesBackendApplication>(*args)
}
