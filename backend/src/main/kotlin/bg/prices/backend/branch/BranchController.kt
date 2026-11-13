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
    fun inLocation(
        @RequestParam(required = false) locality: String?,
        @RequestParam(required = false) postalCode: String?
    ): List<BranchResponse> {
        if (!locality.isNullOrBlank() && !postalCode.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose locality or postal code.")
        }
        return when {
            !locality.isNullOrBlank() -> service.inLocality(locality)
            !postalCode.isNullOrBlank() -> service.inPostalCode(postalCode)
            else -> emptyList()
        }
    }
}
