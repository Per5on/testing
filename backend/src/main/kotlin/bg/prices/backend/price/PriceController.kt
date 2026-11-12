package bg.prices.backend.price

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/prices")
class PriceController(private val jdbc: JdbcTemplate) {
    @GetMapping("/latest")
    fun latest(@RequestParam productId: UUID): List<PriceResponse> = jdbc.query(
        """
        SELECT DISTINCT ON (COALESCE(p.branch_id::text, 'ONLINE:' || p.retailer_code))
            p.id, p.product_id, p.branch_id, COALESCE(b.retailer_name, p.retailer_name) AS retailer_name, b.branch_name,
            b.city, b.settlement, b.address, p.price_eur, p.regular_price_eur,
            p.currency, p.source, p.verified, p.price_scope, p.collected_at, p.valid_until
        FROM price_observations p
        LEFT JOIN store_branches b ON b.id = p.branch_id
        WHERE p.product_id = ?
          AND p.verified = TRUE
          AND (p.valid_from IS NULL OR p.valid_from <= NOW())
          AND (p.valid_until IS NULL OR p.valid_until > NOW())
          AND (p.price_scope = 'NATIONAL_ONLINE' OR b.active = TRUE)
        ORDER BY COALESCE(p.branch_id::text, 'ONLINE:' || p.retailer_code), p.collected_at DESC
        LIMIT 100
        """.trimIndent(),
        { rs, _ ->
            PriceResponse(
                id = rs.getObject("id", UUID::class.java).toString(),
                productId = rs.getObject("product_id", UUID::class.java).toString(),
                branchId = rs.getObject("branch_id", UUID::class.java)?.toString(),
                retailerName = rs.getString("retailer_name"),
                branchName = rs.getString("branch_name"),
                city = rs.getString("city"),
                settlement = rs.getString("settlement"),
                address = rs.getString("address"),
                priceEur = rs.getBigDecimal("price_eur"),
                regularPriceEur = rs.getBigDecimal("regular_price_eur"),
                currency = rs.getString("currency"),
                source = rs.getString("source"),
                verified = rs.getBoolean("verified"),
                priceScope = rs.getString("price_scope"),
                collectedAt = rs.getTimestamp("collected_at").toInstant().toString(),
                validUntil = rs.getTimestamp("valid_until")?.toInstant()?.toString()
            )
        },
        productId
    )

    @GetMapping("/catalog")
    fun catalog(
        @RequestParam(required = false) categoryCode: String?,
        @RequestParam(required = false) locality: String?,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(defaultValue = "10") radiusKm: Int,
        @RequestParam(required = false) retailerName: String?,
        @RequestParam(defaultValue = "100") limit: Int
    ): List<PriceCatalogResponse> {
        require(limit in 1..200) { "Limit must be between 1 and 200." }
        require(categoryCode == null || Regex("^[a-z0-9_]{1,40}$").matches(categoryCode)) { "Invalid category code." }
        require(locality == null || locality.trim().length in 2..120) { "Locality must contain 2 to 120 characters." }
        require(retailerName == null || retailerName.trim().length in 2..160) { "Retailer name must contain 2 to 160 characters." }
        require((latitude == null) == (longitude == null)) { "Latitude and longitude must be supplied together." }
        require(latitude == null || latitude in -90.0..90.0) { "Invalid latitude." }
        require(longitude == null || longitude in -180.0..180.0) { "Invalid longitude." }
        require(radiusKm in 1..50) { "Radius must be between 1 and 50 km." }

        val filters = mutableListOf(
            "p.active = TRUE",
            "(po.price_scope = 'NATIONAL_ONLINE' OR b.active = TRUE)",
            "po.verified = TRUE",
            "(po.valid_from IS NULL OR po.valid_from <= NOW())",
            "(po.valid_until IS NULL OR po.valid_until > NOW())"
        )
        val args = mutableListOf<Any>()
        if (!categoryCode.isNullOrBlank()) {
            filters += "p.category_code = ?"
            args += categoryCode
        }
        if (!locality.isNullOrBlank() && latitude == null) {
            filters += "(po.price_scope = 'NATIONAL_ONLINE' OR LOWER(b.city) = LOWER(?) OR LOWER(COALESCE(b.settlement, '')) = LOWER(?))"
            args += locality.trim()
            args += locality.trim()
        }
        if (latitude != null && longitude != null) {
            filters += """
                (po.price_scope = 'NATIONAL_ONLINE' OR (
                b.latitude IS NOT NULL AND b.longitude IS NOT NULL AND
                (6371.0088 * 2 * ASIN(SQRT(
                    POWER(SIN(RADIANS(b.latitude - ?) / 2), 2) +
                    COS(RADIANS(?)) * COS(RADIANS(b.latitude)) *
                    POWER(SIN(RADIANS(b.longitude - ?) / 2), 2)
                ))) <= ?
                ))
            """.trimIndent()
            args += latitude
            args += latitude
            args += longitude
            args += radiusKm.toDouble()
        }
        if (!retailerName.isNullOrBlank()) {
            filters += "LOWER(COALESCE(b.retailer_name, po.retailer_name)) = LOWER(?)"
            args += retailerName.trim()
        }
        args += limit

        val sql = """
            SELECT * FROM (
                SELECT DISTINCT ON (p.id, COALESCE(b.id::text, 'ONLINE:' || po.retailer_code))
                    p.id AS product_id, p.category_code, p.name AS product_name, p.brand,
                    p.variant, p.package_quantity, p.package_unit,
                    COALESCE(po.retailer_code, b.retailer_code) AS retailer_code,
                    COALESCE(po.retailer_name, b.retailer_name) AS retailer_name,
                    b.branch_name, b.city, b.settlement, b.address,
                    po.price_eur, po.regular_price_eur, po.currency, po.source, po.source_reference,
                    po.price_note, po.price_scope, po.verified, po.collected_at, po.valid_until
                FROM price_observations po
                JOIN products p ON p.id = po.product_id
                LEFT JOIN store_branches b ON b.id = po.branch_id
                WHERE ${filters.joinToString(" AND ")}
                ORDER BY p.id, COALESCE(b.id::text, 'ONLINE:' || po.retailer_code), po.collected_at DESC
            ) latest
            ORDER BY product_name, price_eur ASC, retailer_name, city
            LIMIT ?
        """.trimIndent()

        return jdbc.query(sql, { rs, _ ->
            PriceCatalogResponse(
                productId = rs.getObject("product_id", UUID::class.java).toString(),
                categoryCode = rs.getString("category_code"),
                productName = rs.getString("product_name"),
                brand = rs.getString("brand"),
                variant = rs.getString("variant"),
                packageQuantity = rs.getBigDecimal("package_quantity"),
                packageUnit = rs.getString("package_unit"),
                retailerCode = rs.getString("retailer_code"),
                retailerName = rs.getString("retailer_name"),
                branchName = rs.getString("branch_name"),
                city = rs.getString("city"),
                settlement = rs.getString("settlement"),
                address = rs.getString("address"),
                priceEur = rs.getBigDecimal("price_eur"),
                regularPriceEur = rs.getBigDecimal("regular_price_eur"),
                currency = rs.getString("currency"),
                source = rs.getString("source"),
                sourceReference = rs.getString("source_reference"),
                priceNote = rs.getString("price_note"),
                priceScope = rs.getString("price_scope"),
                verified = rs.getBoolean("verified"),
                collectedAt = rs.getTimestamp("collected_at").toInstant().toString(),
                validUntil = rs.getTimestamp("valid_until")?.toInstant()?.toString()
            )
        }, *args.toTypedArray())
    }

    @GetMapping("/history")
    fun history(
        @RequestParam productId: UUID,
        @RequestParam branchId: UUID,
        @RequestParam(defaultValue = "30") limit: Int
    ): List<PriceHistoryResponse> {
        require(limit in 1..100) { "Limit must be between 1 and 100." }
        return jdbc.query(
            """
            SELECT id, price_eur, currency, source, verified, collected_at
            FROM price_observations
            WHERE product_id = ? AND branch_id = ?
            ORDER BY collected_at DESC
            LIMIT ?
            """.trimIndent(),
            { rs, _ ->
                PriceHistoryResponse(
                    id = rs.getObject("id", UUID::class.java).toString(),
                    priceEur = rs.getBigDecimal("price_eur"),
                    currency = rs.getString("currency"),
                    source = rs.getString("source"),
                    verified = rs.getBoolean("verified"),
                    collectedAt = rs.getTimestamp("collected_at").toInstant().toString()
                )
            },
            productId, branchId, limit
        )
    }
}

data class PriceResponse(
    val id: String,
    val productId: String,
    val branchId: String?,
    val retailerName: String,
    val branchName: String?,
    val city: String?,
    val settlement: String?,
    val address: String,
    val priceEur: BigDecimal,
    val regularPriceEur: BigDecimal?,
    val currency: String,
    val source: String,
    val verified: Boolean,
    val collectedAt: String,
    val validUntil: String?
)

data class PriceCatalogResponse(
    val productId: String,
    val categoryCode: String,
    val productName: String,
    val brand: String?,
    val variant: String?,
    val packageQuantity: BigDecimal?,
    val packageUnit: String?,
    val retailerCode: String,
    val retailerName: String,
    val branchName: String?,
    val city: String,
    val settlement: String?,
    val address: String?,
    val priceEur: BigDecimal,
    val regularPriceEur: BigDecimal?,
    val currency: String,
    val source: String,
    val sourceReference: String?,
    val priceNote: String?,
    val priceScope: String,
    val verified: Boolean,
    val collectedAt: String,
    val validUntil: String?
)

data class PriceHistoryResponse(
    val id: String,
    val priceEur: BigDecimal,
    val currency: String,
    val source: String,
    val verified: Boolean,
    val collectedAt: String
)
