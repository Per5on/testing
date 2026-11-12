package bg.prices.backend.catalog

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/catalog")
class CatalogController(private val jdbc: JdbcTemplate) {

    @GetMapping
    @Cacheable(cacheNames = ["catalog"], key = "#locality + ':' + #latitude + ':' + #longitude + ':' + #radiusKm")
    fun catalog(
        @RequestParam(required = false) locality: String?,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(defaultValue = "10") radiusKm: Double = 10.0
    ): CatalogResponse {
        require((latitude == null) == (longitude == null)) { "Latitude and longitude must be supplied together." }
        if (latitude != null) {
            require(latitude in -90.0..90.0 && longitude!! in -180.0..180.0) { "Invalid coordinates." }
            require(radiusKm in 1.0..200.0) { "Radius must be between 1 and 200 km." }
        }

        val categories = jdbc.query(
            """
            SELECT code, name_bg, name_en, description_bg, description_en
            FROM product_categories WHERE active = TRUE ORDER BY sort_order
            """.trimIndent()
        ) { rs, _ ->
            val code = rs.getString("code")
            CatalogCategory(
                code = code,
                nameBg = rs.getString("name_bg"),
                nameEn = rs.getString("name_en"),
                descriptionBg = rs.getString("description_bg"),
                descriptionEn = rs.getString("description_en"),
                retailers = availableRetailers(code, locality?.trim()?.takeIf { it.isNotBlank() }, latitude, longitude, radiusKm)
            )
        }
        return CatalogResponse(categories = categories, generatedAt = Instant.now().toString(), branchDataSource = "OpenStreetMap contributors", branchRefreshIntervalHours = 12)
    }

    private fun availableRetailers(
        categoryCode: String,
        locality: String?,
        latitude: Double?,
        longitude: Double?,
        radiusKm: Double
    ): List<CatalogRetailer> {
        val locationClause: String
        val args = mutableListOf<Any>(categoryCode)
        if (locality != null) {
            locationClause = """
                AND (
                  LOWER(b.city) = LOWER(?)
                  OR LOWER(COALESCE(b.settlement, '')) = LOWER(?)
                  OR EXISTS (
                    SELECT 1
                    FROM inhabited_localities l
                    WHERE (LOWER(l.name) = LOWER(?) OR LOWER(COALESCE(l.name_bg, '')) = LOWER(?))
                      AND (
                        LOWER(b.city) = LOWER(l.name)
                        OR LOWER(COALESCE(b.settlement, '')) = LOWER(l.name)
                        OR (l.name_bg IS NOT NULL AND LOWER(b.city) = LOWER(l.name_bg))
                        OR (l.name_bg IS NOT NULL AND LOWER(COALESCE(b.settlement, '')) = LOWER(l.name_bg))
                      )
                  )
                )
            """.trimIndent()
            args += locality
            args += locality
            args += locality
            args += locality
        } else if (latitude != null && longitude != null) {
            locationClause = """
                AND (6371.0088 * acos(LEAST(1.0, GREATEST(-1.0,
                    cos(radians(?)) * cos(radians(b.latitude)) *
                    cos(radians(b.longitude) - radians(?)) +
                    sin(radians(?)) * sin(radians(b.latitude))
                )))) <= ?
            """.trimIndent()
            args += latitude
            args += longitude
            args += latitude
            args += radiusKm
        } else {
            // No location is selected: don't claim physical branch availability.
            // National online offers remain discoverable without a fabricated branch.
            locationClause = "AND FALSE"
        }

        return jdbc.query(
            """
            SELECT r.code, r.name, r.website_url
            FROM catalog_retailers r
            WHERE r.active = TRUE AND r.category_code = ?
              AND (
                EXISTS (
                    SELECT 1 FROM store_branches b
                    WHERE b.active = TRUE AND LOWER(b.retailer_code) = LOWER(r.code)
                    $locationClause
                )
                OR EXISTS (
                    SELECT 1
                    FROM price_observations po
                    JOIN products p ON p.id = po.product_id
                    WHERE po.price_scope = 'NATIONAL_ONLINE'
                      AND LOWER(po.retailer_code) = LOWER(r.code)
                      AND p.category_code = r.category_code
                      AND po.verified = TRUE
                      AND (po.valid_from IS NULL OR po.valid_from <= NOW())
                      AND (po.valid_until IS NULL OR po.valid_until > NOW())
                )
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
