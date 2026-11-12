package bg.prices.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BgPricesBackendApplication

fun main(args: Array<String>) {
    runApplication<BgPricesBackendApplication>(*args)
}
