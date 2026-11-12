package bg.prices.backend.product

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val jdbc: JdbcTemplate) {
    @GetMapping
    fun search(
        @RequestParam(required = false) categoryCode: String?,
        @RequestParam(required = false) query: String?,
        @RequestParam(defaultValue = "30") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ProductPage {
        require(limit in 1..100) { "Limit must be between 1 and 100." }
        require(offset in 0..10000) { "Offset must be between 0 and 10000." }
        val term = query?.trim()?.takeIf { it.isNotBlank() }
        require(term == null || term.length in 2..80) { "Search text must contain 2 to 80 characters." }
        val category = categoryCode?.trim()?.takeIf { it.isNotBlank() }
        require(category == null || category.matches(Regex("[a-z0-9_]{2,40}"))) { "Invalid category code." }

        val where = buildString {
            append("WHERE p.active = TRUE ")
            if (category != null) append("AND p.category_code = ? ")
            if (term != null) append("AND (p.name ILIKE ? OR COALESCE(p.brand, '') ILIKE ? OR COALESCE(p.barcode, '') = ?) ")
        }
        val args = mutableListOf<Any>()
        if (category != null) args += category
        if (term != null) {
            args += "%$term%"
            args += "%$term%"
            args += term
        }
        val total = jdbc.queryForObject(
            "SELECT COUNT(*) FROM products p $where",
            Long::class.javaObjectType,
            *args.toTypedArray()
        ) ?: 0L
        val rows = jdbc.query(
            """
            SELECT p.id, p.category_code, p.barcode, p.brand, p.name, p.variant,
                   p.package_quantity, p.package_unit
            FROM products p
            $where
            ORDER BY p.name, p.brand NULLS LAST
            LIMIT ? OFFSET ?
            """.trimIndent(),
            { rs, _ ->
                ProductResponse(
                    id = rs.getObject("id", UUID::class.java).toString(),
                    categoryCode = rs.getString("category_code"),
                    barcode = rs.getString("barcode"),
                    brand = rs.getString("brand"),
                    name = rs.getString("name"),
                    variant = rs.getString("variant"),
                    packageQuantity = rs.getBigDecimal("package_quantity"),
                    packageUnit = rs.getString("package_unit")
                )
            },
            *(args + listOf(limit, offset)).toTypedArray()
        )
        return ProductPage(rows, total, limit, offset)
    }
}

data class ProductResponse(
    val id: String,
    val categoryCode: String,
    val barcode: String?,
    val brand: String?,
    val name: String,
    val variant: String?,
    val packageQuantity: BigDecimal?,
    val packageUnit: String?
)

data class ProductPage(
    val items: List<ProductResponse>,
    val total: Long,
    val limit: Int,
    val offset: Int
)
