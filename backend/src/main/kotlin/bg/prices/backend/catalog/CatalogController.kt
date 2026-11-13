package bg.prices.backend.catalog

import org.springframework.cache.annotation.Cacheable
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(private val jdbc: JdbcTemplate) {
    @GetMapping
    @Cacheable(cacheNames = ["catalog"], key = "#locality + ':' + #postalCode")
    fun catalog(
        @RequestParam(required = false) locality: String?,
        @RequestParam(required = false) postalCode: String?
    ): CatalogResponse {
        val normalizedLocality = locality?.trim()?.takeIf { it.isNotBlank() }
        val normalizedPostalCode = postalCode?.filterNot(Char::isWhitespace)?.takeIf { it.isNotBlank() }
        val locationModes = listOf(normalizedLocality != null, normalizedPostalCode != null).count { it }
        require(locationModes <= 1) { "Choose either a settlement or a postal code." }
        if (normalizedPostalCode != null) {
            require(normalizedPostalCode.matches(Regex("^[0-9]{4}$"))) {
                "Bulgarian postal codes must contain exactly four digits."
            }
        }

        val categories = jdbc.query(
            """
            SELECT code, name_bg, name_en, description_bg, description_en
            FROM product_categories
            WHERE active = TRUE
            ORDER BY sort_order
            """.trimIndent()
        ) { rs, _ ->
            val code = rs.getString("code")
            CatalogCategory(
                code = code,
                nameBg = rs.getString("name_bg"),
                nameEn = rs.getString("name_en"),
                descriptionBg = rs.getString("description_bg"),
                descriptionEn = rs.getString("description_en"),
                retailers = availableRetailers(code, normalizedLocality, normalizedPostalCode)
            )
        }
        return CatalogResponse(
            categories = categories,
            generatedAt = Instant.now().toString(),
            branchDataSource = "Verified official retailer websites and apps",
            branchRefreshIntervalHours = 24
        )
    }

    private fun availableRetailers(
        categoryCode: String,
        locality: String?,
        postalCode: String?
    ): List<CatalogRetailer> {
        val locationClause: String
        val args = mutableListOf<Any>(categoryCode)
        when {
            locality != null -> {
                locationClause = """
                    AND (
                      LOWER(b.city) = LOWER(?)
                      OR LOWER(COALESCE(b.settlement, '')) = LOWER(?)
                    )
                """.trimIndent()
                args += locality
                args += locality
            }
            postalCode != null -> {
                locationClause = "AND b.postal_code = ?"
                args += postalCode
            }
            else -> return emptyList()
        }

        return jdbc.query(
            """
            SELECT r.code, r.name, r.website_url
            FROM catalog_retailers r
            WHERE r.active = TRUE
              AND r.category_code = ?
              AND EXISTS (
                SELECT 1
                FROM store_branches b
                WHERE b.active = TRUE
                  AND LOWER(b.retailer_code) = LOWER(r.code)
                  AND b.source_type IN ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
                  AND b.source_url IS NOT NULL
                  AND b.source_verified_at IS NOT NULL
                  $locationClause
              )
            ORDER BY r.name
            """.trimIndent(),
            { rs, _ ->
                CatalogRetailer(
                    code = rs.getString("code"),
                    name = rs.getString("name"),
                    websiteUrl = rs.getString("website_url")
                )
            },
            *args.toTypedArray()
        )
    }
}

data class CatalogResponse(
    val categories: List<CatalogCategory>,
    val generatedAt: String,
    val branchDataSource: String,
    val branchRefreshIntervalHours: Int
)

data class CatalogCategory(
    val code: String,
    val nameBg: String,
    val nameEn: String,
    val descriptionBg: String,
    val descriptionEn: String,
    val retailers: List<CatalogRetailer>
)

data class CatalogRetailer(
    val code: String,
    val name: String,
    val websiteUrl: String?
)
