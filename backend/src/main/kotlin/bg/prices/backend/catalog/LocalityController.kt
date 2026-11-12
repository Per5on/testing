package bg.prices.backend.catalog

import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/localities")
class LocalityController(private val jdbc: JdbcTemplate) {

    @GetMapping
    @Cacheable(cacheNames = ["localities"], key = "#query.trim().toLowerCase()")
    fun search(@RequestParam query: String): List<LocalitySuggestionResponse> {
        val term = query.trim()
        if (term.length !in 2..80) return emptyList()
        val pattern = term.lowercase() + "%"
        return jdbc.query(
            """
            SELECT id, name, name_bg, type, municipality, region
            FROM inhabited_localities
            WHERE on_map = TRUE
              AND (LOWER(name) LIKE ? OR LOWER(COALESCE(name_bg, '')) LIKE ?)
            ORDER BY
              CASE WHEN LOWER(name) = LOWER(?) OR LOWER(COALESCE(name_bg, '')) = LOWER(?) THEN 0 ELSE 1 END,
              name
            LIMIT 15
            """.trimIndent(),
            { rs, _ ->
                LocalitySuggestionResponse(
                    id = rs.getObject("id", UUID::class.java).toString(),
                    name = rs.getString("name"),
                    nameBg = rs.getString("name_bg"),
                    type = rs.getString("type"),
                    municipality = rs.getString("municipality"),
                    region = rs.getString("region")
                )
            },
            pattern, pattern, term, term
        )
    }
}

data class LocalitySuggestionResponse(
    val id: String,
    val name: String,
    val nameBg: String?,
    val type: String,
    val municipality: String?,
    val region: String?
)
