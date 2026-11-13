package bg.prices.backend.catalog

import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/localities")
class LocalityController(private val jdbc: JdbcTemplate) {
    @GetMapping
    @Cacheable(cacheNames = ["localities"], key = "#query.trim().toLowerCase() + ':' + #mode")
    fun search(
        @RequestParam query: String,
        @RequestParam(defaultValue = "locality") mode: String
    ): List<LocalitySuggestionResponse> {
        val term = query.trim()
        require(mode == "locality" || mode == "postalCode") { "Mode must be locality or postalCode." }
        if (mode == "postalCode") {
            if (term.length !in 1..4 || term.any { !it.isDigit() }) return emptyList()
            return jdbc.query(
                """
                SELECT MIN(b.id::text) AS id, b.postal_code AS name,
                       MIN(b.city) AS name_bg, 'POSTAL_CODE' AS type,
                       MIN(b.city) AS municipality, MIN(b.region) AS region,
                       b.postal_code AS postal_code
                FROM store_branches b
                WHERE b.active = TRUE
                  AND b.postal_code LIKE ?
                  AND b.source_type IN ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
                  AND b.source_url IS NOT NULL
                  AND b.source_verified_at IS NOT NULL
                GROUP BY b.postal_code
                ORDER BY b.postal_code
                LIMIT 15
                """.trimIndent(),
                { rs, _ -> rs.toSuggestion() },
                term + "%"
            )
        }
        if (term.length !in 2..80) return emptyList()
        val pattern = "%" + term.lowercase() + "%"
        return jdbc.query(
            """
            SELECT MIN(b.id::text) AS id,
                   COALESCE(NULLIF(b.settlement, ''), b.city) AS name,
                   COALESCE(NULLIF(b.settlement, ''), b.city) AS name_bg,
                   MIN(b.locality_type) AS type,
                   MIN(b.municipality) AS municipality,
                   MIN(b.region) AS region,
                   MIN(b.postal_code) AS postal_code
            FROM store_branches b
            WHERE b.active = TRUE
              AND (
                LOWER(b.city) LIKE ?
                OR LOWER(COALESCE(b.settlement, '')) LIKE ?
                OR LOWER(COALESCE(b.municipality, '')) LIKE ?
              )
              AND b.source_type IN ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
              AND b.source_url IS NOT NULL
              AND b.source_verified_at IS NOT NULL
            GROUP BY COALESCE(NULLIF(b.settlement, ''), b.city)
            ORDER BY CASE WHEN LOWER(COALESCE(NULLIF(b.settlement, ''), b.city)) = LOWER(?) THEN 0 ELSE 1 END,
                     name
            LIMIT 15
            """.trimIndent(),
            { rs, _ -> rs.toSuggestion() },
            pattern, pattern, pattern, term
        )
    }
}

private fun java.sql.ResultSet.toSuggestion() = LocalitySuggestionResponse(
    id = getString("id"),
    name = getString("name"),
    nameBg = getString("name_bg"),
    type = getString("type") ?: "",
    municipality = getString("municipality"),
    region = getString("region"),
    postalCode = getString("postal_code")
)

data class LocalitySuggestionResponse(
    val id: String,
    val name: String,
    val nameBg: String?,
    val type: String,
    val municipality: String?,
    val region: String?,
    val postalCode: String?
)
