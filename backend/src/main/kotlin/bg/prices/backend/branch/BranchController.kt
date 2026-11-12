package bg.prices.backend.branch

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/branches")
class BranchController(private val service: BranchService) {
    @GetMapping
    fun inLocality(@RequestParam locality: String) = service.inLocality(locality)

    @GetMapping("/localities")
    fun searchLocalities(@RequestParam query: String) = service.searchLocalities(query)

    @GetMapping("/nearby")
    fun nearby(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "10") radiusKm: Double
    ) = try {
        service.nearby(latitude, longitude, radiusKm)
    } catch (exception: IllegalArgumentException) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, exception.message)
    }
}
