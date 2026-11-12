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
        SELECT DISTINCT ON (p.branch_id)
            p.id, p.product_id, p.branch_id, b.retailer_name, b.branch_name,
            b.city, b.settlement, b.address, p.price_eur, p.regular_price_eur,
            p.currency, p.source, p.verified, p.collected_at, p.valid_until
        FROM price_observations p
        JOIN store_branches b ON b.id = p.branch_id
        WHERE p.product_id = ?
          AND p.verified = TRUE
          AND (p.valid_from IS NULL OR p.valid_from <= NOW())
          AND (p.valid_until IS NULL OR p.valid_until > NOW())
          AND b.active = TRUE
        ORDER BY p.branch_id, p.collected_at DESC
        LIMIT 100
        """.trimIndent(),
        { rs, _ ->
            PriceResponse(
                id = rs.getObject("id", UUID::class.java).toString(),
                productId = rs.getObject("product_id", UUID::class.java).toString(),
                branchId = rs.getObject("branch_id", UUID::class.java).toString(),
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
                collectedAt = rs.getTimestamp("collected_at").toInstant().toString(),
                validUntil = rs.getTimestamp("valid_until")?.toInstant()?.toString()
            )
        },
        productId
    )

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
    val branchId: String,
    val retailerName: String,
    val branchName: String?,
    val city: String,
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

data class PriceHistoryResponse(
    val id: String,
    val priceEur: BigDecimal,
    val currency: String,
    val source: String,
    val verified: Boolean,
    val collectedAt: String
)
