package bg.prices.backend.catalog

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenStreetMapImportJob(
    private val jdbc: JdbcTemplate,
    private val mapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val requestFactory = SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(10_000)
        setReadTimeout(240_000)
    }
    private val client = RestClient.builder()
        .requestFactory(requestFactory)
        .defaultHeader("User-Agent", "BGPrices/0.1-preview (https://github.com/Per5on/testing)")
        .build()

    private val retailers = listOf(
        RetailerMatch("kaufland", "Kaufland", listOf("kaufland")),
        RetailerMatch("lidl", "Lidl", listOf("lidl")),
        RetailerMatch("billa", "BILLA", listOf("billa")),
        RetailerMatch("fantastico", "Fantastico", listOf("fantastico")),
        RetailerMatch("t_market", "T MARKET", listOf("t market", "t-market", "tmarket")),
        RetailerMatch("metro", "METRO", listOf("metro")),
        RetailerMatch("moyat_magazin", "Моят магазин", listOf("moyat magazin", "my store")),
        RetailerMatch("cba", "CBA", listOf("cba")),
        RetailerMatch("promarket", "ProMarket", listOf("promarket", "pro market")),
        RetailerMatch("hit_max", "HIT Max", listOf("hit max", "hitmax")),
        RetailerMatch("burlex", "Burlex", listOf("burlex")),
        RetailerMatch("klasiko", "Klasiko", listOf("klasiko")),
        RetailerMatch("abc_market", "ABC Market", listOf("abc market", "abcmarket")),
        RetailerMatch("mini_mart", "Mini Mart", listOf("mini mart", "minimart")),
        RetailerMatch("technopolis", "Technopolis", listOf("technopolis")),
        RetailerMatch("technomarket", "Technomarket", listOf("technomarket")),
        RetailerMatch("zora", "Зора", listOf("zora")),
        RetailerMatch("ardes", "Ardes", listOf("ardes")),
        RetailerMatch("plesio", "Plesio", listOf("plesio")),
        RetailerMatch("plasico", "Plasico", listOf("plasico")),
        RetailerMatch("vali", "Вали", listOf("vali")),
        RetailerMatch("jar_computers", "Jar Computers", listOf("jar computers", "jarcomputers")),
        RetailerMatch("homemax", "HomeMax", listOf("homemax", "home max")),
        RetailerMatch("bauhaus", "Bauhaus", listOf("bauhaus")),
        RetailerMatch("ikea", "IKEA", listOf("ikea")),
        RetailerMatch("jysk", "JYSK", listOf("jysk")),
        RetailerMatch("mr_bricolage", "Mr. Bricolage", listOf("mr bricolage", "mrbricolage")),
        RetailerMatch("praktiker", "Praktiker", listOf("praktiker")),
        RetailerMatch("praktis", "Практис", listOf("praktis")),
        RetailerMatch("videnov", "Мебели Виденов", listOf("videnov", "mebeli videnov")),
        RetailerMatch("mondo", "Mondo", listOf("mondo")),
        RetailerMatch("lukoil", "LUKOIL", listOf("lukoil")),
        RetailerMatch("omv", "OMV", listOf("omv")),
        RetailerMatch("shell", "Shell", listOf("shell")),
        RetailerMatch("eko", "EKO", listOf("eko")),
        RetailerMatch("petrol", "Petrol", listOf("petrol")),
        RetailerMatch("rompetrol", "Rompetrol", listOf("rompetrol")),
        RetailerMatch("insa_oil", "Insa Oil", listOf("insa oil", "insaoil"))
    )

    @Transactional
    fun importOnce() {
        try {
            val query = """
                [out:json][timeout:180];
                area["ISO3166-1"="BG"][admin_level=2]->.bg;
                (
                  nwr["place"~"^(city|town|village|hamlet|isolated_dwelling|suburb|neighbourhood|quarter)$"](area.bg);
                  nwr["shop"~"^(supermarket|convenience|electronics|computer|appliance|furniture|homeware|hardware|doityourself|department_store)$"](area.bg);
                  nwr["amenity"="fuel"](area.bg);
                );
                out center tags;
            """.trimIndent()
            val response = client.post()
                .uri("https://overpass-api.de/api/interpreter")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .body("data=" + java.net.URLEncoder.encode(query, Charsets.UTF_8))
                .retrieve()
                .body(String::class.java)
                ?: error("Empty Overpass response")
            val elements = mapper.readTree(response).path("elements")
            var localitiesImported = 0
            var branchesImported = 0
            elements.forEach { element ->
                val tags = element.path("tags")
                if (tags.isMissingNode || !tags.isObject) return@forEach
                val sourceId = "${element.path("type").asText()}/${element.path("id").asText()}"
                val lat = coordinate(element, "lat")
                val lon = coordinate(element, "lon")
                if (lat == null || lon == null) return@forEach

                val place = tags.path("place").asText("")
                val name = tags.path("name").asText("").trim()
                if (place.isNotBlank() && name.isNotBlank()) {
                    upsertLocality(sourceId, name, tags, place, lat, lon)
                    localitiesImported++
                }

                val candidate = listOf("brand", "brand:en", "operator", "name")
                    .map { tags.path(it).asText("") }
                    .firstOrNull { value -> matchRetailer(value) != null }
                    ?: return@forEach
                val retailer = matchRetailer(candidate) ?: return@forEach
                val city = firstTag(tags, "addr:city", "addr:town", "addr:village", "addr:place")
                    ?: return@forEach
                val settlement = firstTag(tags, "addr:place", "addr:village", "addr:town") ?: city
                val street = firstTag(tags, "addr:street", "addr:place")
                val houseNumber = firstTag(tags, "addr:housenumber")
                val postcode = firstTag(tags, "addr:postcode")
                val address = listOfNotNull(street, houseNumber, postcode).joinToString(" ").ifBlank { city }
                upsertBranch(
                    sourceId = sourceId,
                    retailer = retailer,
                    branchName = name.ifBlank { retailer.name },
                    city = city,
                    settlement = settlement,
                    municipality = firstTag(tags, "addr:municipality"),
                    region = firstTag(tags, "addr:province", "is_in:state"),
                    address = address,
                    latitude = lat,
                    longitude = lon
                )
                branchesImported++
            }
            log.info("OpenStreetMap import complete: {} populated places, {} matched retailer branches", localitiesImported, branchesImported)
        } catch (exception: Exception) {
            log.warn("OpenStreetMap import failed; existing catalogue data is retained: {}", exception.message)
        }
    }

    private fun coordinate(element: JsonNode, field: String): Double? {
        val direct = element.path(field)
        val value = if (!direct.isMissingNode && !direct.isNull) direct else element.path("center").path(field)
        return value.takeIf { !it.isMissingNode && !it.isNull }?.asDouble()
            ?.takeIf { it.isFinite() }
    }

    private fun firstTag(tags: JsonNode, vararg keys: String): String? =
        keys.asSequence().map { tags.path(it).asText("").trim() }.firstOrNull { it.isNotBlank() }

    private fun upsertLocality(
        sourceId: String,
        name: String,
        tags: JsonNode,
        place: String,
        latitude: Double,
        longitude: Double
    ) {
        val type = when (place) {
            "city" -> "CITY"
            "town" -> "TOWN"
            "village" -> "VILLAGE"
            else -> "OTHER"
        }
        jdbc.update(
            """
            INSERT INTO inhabited_localities
              (source, source_id, name, name_bg, type, municipality, region, latitude, longitude, on_map)
            VALUES ('OSM', ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
            ON CONFLICT (source, source_id) DO UPDATE SET
              name = EXCLUDED.name, name_bg = EXCLUDED.name_bg, type = EXCLUDED.type,
              municipality = EXCLUDED.municipality, region = EXCLUDED.region,
              latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude, on_map = TRUE
            """.trimIndent(),
            sourceId, name, tags.path("name:bg").asText(null), type,
            firstTag(tags, "addr:municipality"), firstTag(tags, "addr:province", "is_in:state"),
            latitude, longitude
        )
    }

    private fun upsertBranch(
        sourceId: String,
        retailer: RetailerMatch,
        branchName: String,
        city: String,
        settlement: String,
        municipality: String?,
        region: String?,
        address: String,
        latitude: Double,
        longitude: Double
    ) {
        jdbc.update(
            """
            INSERT INTO store_branches
              (retailer_code, retailer_name, branch_name, city, settlement, locality_type,
               municipality, region, address, latitude, longitude, active, source, source_id, last_seen_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 'OTHER', ?, ?, ?, ?, ?, TRUE, 'OSM', ?, NOW(), NOW())
            ON CONFLICT (source, source_id) DO UPDATE SET
              retailer_code = EXCLUDED.retailer_code, retailer_name = EXCLUDED.retailer_name,
              branch_name = EXCLUDED.branch_name, city = EXCLUDED.city, settlement = EXCLUDED.settlement,
              municipality = EXCLUDED.municipality, region = EXCLUDED.region, address = EXCLUDED.address,
              latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude, active = TRUE,
              last_seen_at = NOW(), updated_at = NOW()
            """.trimIndent(),
            retailer.code, retailer.name, branchName, city, settlement, municipality, region,
            address, latitude, longitude, sourceId
        )
    }

    private fun matchRetailer(value: String): RetailerMatch? {
        val normalized = normalize(value)
        return retailers.firstOrNull { match -> match.aliases.any { normalized.contains(normalize(it)) } }
    }

    private fun normalize(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }

    private data class RetailerMatch(val code: String, val name: String, val aliases: List<String>)
}
