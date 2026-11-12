package bg.prices.backend.locality

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/localities")
class LocalityController(private val repository: LocalityRepository) {
    @GetMapping
    fun search(@RequestParam query: String): List<LocalityResponse> {
        val term = query.trim()
        if (term.length < 2) return emptyList()
        return repository.searchOnMap(term).take(50).map { it.toResponse() }
    }
}
