package bg.prices.backend.catalog

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OpenStreetMapImportJob(
    private val jdbc: JdbcTemplate,
    private val mapper: ObjectMapper,
    @Value("\${catalog.osm.overpass-url}") private val overpassUrl: String
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
        RetailerMatch("moyat_magazin", "Моят магазин", listOf("moyat magazin", "моят магазин", "my store")),
        RetailerMatch("cba", "CBA", listOf("cba")),
        RetailerMatch("promarket", "ProMarket", listOf("promarket", "pro market", "промаркет")),
        RetailerMatch("hit_max", "HIT Max", listOf("hit max", "hitmax", "хит макс")),
        RetailerMatch("burlex", "Burlex", listOf("burlex", "бурлекс")),
        RetailerMatch("klasiko", "Klasiko", listOf("klasiko", "класико")),
        RetailerMatch("abc_market", "ABC Market", listOf("abc market", "abcmarket")),
        RetailerMatch("mini_mart", "Mini Mart", listOf("mini mart", "minimart", "мини март")),
        RetailerMatch("technopolis", "Technopolis", listOf("technopolis", "технополис")),
        RetailerMatch("technomarket", "Technomarket", listOf("technomarket", "техномаркет")),
        RetailerMatch("zora", "Зора", listOf("zora", "зора")),
        RetailerMatch("ardes", "Ardes", listOf("ardes")),
        RetailerMatch("plesio", "Plesio", listOf("plesio")),
        RetailerMatch("plasico", "Plasico", listOf("plasico")),
        RetailerMatch("vali", "Вали", listOf("vali", "вали")),
        RetailerMatch("jar_computers", "Jar Computers", listOf("jar computers", "jarcomputers")),
        RetailerMatch("homemax", "HomeMax", listOf("homemax", "home max")),
        RetailerMatch("bauhaus", "Bauhaus", listOf("bauhaus")),
        RetailerMatch("ikea", "IKEA", listOf("ikea")),
        RetailerMatch("jysk", "JYSK", listOf("jysk")),
        RetailerMatch("mr_bricolage", "Mr. Bricolage", listOf("mr bricolage", "mrbricolage")),
        RetailerMatch("praktiker", "Praktiker", listOf("praktiker")),
        RetailerMatch("praktis", "Практис", listOf("praktis", "практис")),
        RetailerMatch("videnov", "Мебели Виденов", listOf("videnov", "mebeli videnov", "мебели виденов", "виденов")),
        RetailerMatch("mondo", "Mondo", listOf("mondo", "мондо")),
        RetailerMatch("lukoil", "LUKOIL", listOf("lukoil")),
        RetailerMatch("omv", "OMV", listOf("omv")),
        RetailerMatch("shell", "Shell", listOf("shell")),
        RetailerMatch("eko", "EKO", listOf("eko")),
        RetailerMatch("petrol", "Petrol", listOf("petrol")),
        RetailerMatch("rompetrol", "Rompetrol", listOf("rompetrol")),
        RetailerMatch("insa_oil", "Insa Oil", listOf("insa oil", "insaoil"))
    )

    fun importOnce() {
        val startedAt = System.nanoTime()
        try {
            val localityQuery = """
                [out:json][timeout:120];
                area["ISO3166-1"="BG"][admin_level=2]->.bg;
                nwr["place"~"^(city|town|village|hamlet)$"](area.bg);
                out center tags;
            """.trimIndent()
            val retailerPattern = retailers.flatMap { it.aliases }.distinct()
                .joinToString("|") { it.replace(".", "\\.") }
            val branchQuery = """
                [out:json][timeout:120];
                area["ISO3166-1"="BG"][admin_level=2]->.bg;
                (
                  nwr["name"~"(?i)($retailerPattern)"](area.bg);
                  nwr["brand"~"(?i)($retailerPattern)"](area.bg);
                  nwr["brand:en"~"(?i)($retailerPattern)"](area.bg);
                  nwr["operator"~"(?i)($retailerPattern)"](area.bg);
                );
                out center tags;
            """.trimIndent()

            // Keep the country-wide queries small: settlement names and only the
            // retailer brands already present in the BG Prices catalogue.
            val localityElements = fetchElements(localityQuery, "inhabited places")
            val branchElements = fetchElements(branchQuery, "known retailer branches")
            var localitiesImported = 0
            var branchesImported = 0
            var branchesSkipped = 0

            localityElements.forEach { element ->
                val tags = element.path("tags")
                if (tags.isMissingNode || !tags.isObject) return@forEach
                val place = tags.path("place").asText("")
                val name = tags.path("name").asText("").trim().take(180)
                val lat = coordinate(element, "lat")
                val lon = coordinate(element, "lon")
                if (place.isNotBlank() && name.isNotBlank() && lat != null && lon != null) {
                    upsertLocality(sourceId(element), name, tags, place, lat, lon)
                    localitiesImported++
                }
            }

            branchElements.forEach { element ->
                val tags = element.path("tags")
                if (tags.isMissingNode || !tags.isObject) return@forEach
                val lat = coordinate(element, "lat")
                val lon = coordinate(element, "lon")
                if (lat == null || lon == null) {
                    branchesSkipped++
                    return@forEach
                }
                val candidate = listOf("brand", "brand:en", "operator", "name")
                    .map { tags.path(it).asText("") }
                    .firstOrNull { value -> matchRetailer(value) != null }
                    ?: return@forEach
                val retailer = matchRetailer(candidate) ?: return@forEach
                val nearest = nearestLocality(lat, lon)
                val city = (firstTag(tags, "addr:city", "addr:town", "addr:village", "addr:place")
                    ?: nearest?.name)?.take(120)
                if (city.isNullOrBlank()) {
                    branchesSkipped++
                    return@forEach
                }
                val settlement = (firstTag(tags, "addr:place", "addr:village", "addr:town") ?: city).take(160)
                val street = firstTag(tags, "addr:street")
                val houseNumber = firstTag(tags, "addr:housenumber")
                val postcode = firstTag(tags, "addr:postcode")
                val address = (firstTag(tags, "addr:full")
                    ?: listOfNotNull(street, houseNumber).joinToString(" ").ifBlank { city }).take(300)
                val localityType = if (nearest != null && nearest.name.equals(city, ignoreCase = true)) nearest.type else "OTHER"
                upsertBranch(
                    sourceId = sourceId(element),
                    retailer = retailer,
                    branchName = tags.path("name").asText("").trim().ifBlank { retailer.name }.take(160),
                    city = city,
                    settlement = settlement,
                    localityType = localityType,
                    municipality = (firstTag(tags, "addr:municipality") ?: nearest?.municipality)?.take(120),
                    region = (firstTag(tags, "addr:province", "is_in:state") ?: nearest?.region)?.take(120),
                    address = listOfNotNull(address, postcode?.takeIf { !address.contains(it) }).joinToString(", ").take(300),
                    latitude = lat,
                    longitude = lon
                )
                branchesImported++
            }
            log.info(
                "OpenStreetMap import complete: {} populated places, {} matched retailer branches, {} skipped candidates in {} ms",
                localitiesImported, branchesImported, branchesSkipped, (System.nanoTime() - startedAt) / 1_000_000
            )
        } catch (exception: Exception) {
            log.error(
                "OpenStreetMap import failed after {} ms; existing catalogue data is retained",
                (System.nanoTime() - startedAt) / 1_000_000,
                exception
            )
            throw exception
        }
    }

    private fun fetchElements(query: String, description: String): List<JsonNode> {
        log.info("Requesting OpenStreetMap {}", description)
        val response = client.post()
            .uri(overpassUrl)
            .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .body("data=" + java.net.URLEncoder.encode(query, Charsets.UTF_8))
            .retrieve()
            .body(String::class.java)
            ?: error("Empty Overpass response for $description")
        val elements = mapper.readTree(response).path("elements")
        require(elements.isArray) { "Overpass response for $description did not contain an elements array" }
        log.info("Received {} OpenStreetMap elements for {}", elements.size(), description)
        return elements.toList()
    }

    private fun sourceId(element: JsonNode): String =
        "${element.path("type").asText()}/${element.path("id").asText()}"

    private fun coordinate(element: JsonNode, field: String): Double? {
        val direct = element.path(field)
        val value = if (!direct.isMissingNode && !direct.isNull) direct else element.path("center").path(field)
        return value.takeIf { !it.isMissingNode && !it.isNull }?.asDouble()
            ?.takeIf { it.isFinite() }
    }

    private fun firstTag(tags: JsonNode, vararg keys: String): String? =
        keys.asSequence().map { tags.path(it).asText("").trim() }.firstOrNull { it.isNotBlank() }

    private fun nearestLocality(latitude: Double, longitude: Double): LocalityMatch? {
        val latDelta = 0.30
        val lonDelta = 0.45
        return jdbc.query(
            """
            SELECT name, type, municipality, region
            FROM (
                SELECT name, type, municipality, region,
                    (6371.0088 * acos(LEAST(1.0, GREATEST(-1.0,
                        cos(radians(?)) * cos(radians(latitude)) *
                        cos(radians(longitude) - radians(?)) +
                        sin(radians(?)) * sin(radians(latitude))
                    )))) AS distance_km
                FROM inhabited_localities
                WHERE on_map = TRUE
                  AND latitude BETWEEN ? AND ?
                  AND longitude BETWEEN ? AND ?
            ) candidates
            WHERE distance_km <= 25
            ORDER BY distance_km
            LIMIT 1
            """.trimIndent(),
            { rs, _ ->
                LocalityMatch(
                    rs.getString("name"), rs.getString("type"),
                    rs.getString("municipality"), rs.getString("region")
                )
            },
            latitude, longitude, latitude,
            latitude - latDelta, latitude + latDelta,
            longitude - lonDelta, longitude + lonDelta
        ).firstOrNull()
    }

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
            sourceId, name, tags.path("name:bg").asText("").trim().takeIf { it.isNotBlank() }?.take(180), type,
            firstTag(tags, "addr:municipality")?.take(160), firstTag(tags, "addr:province", "is_in:state")?.take(160),
            latitude, longitude
        )
    }

    private fun upsertBranch(
        sourceId: String,
        retailer: RetailerMatch,
        branchName: String,
        city: String,
        settlement: String,
        localityType: String,
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
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE, 'OSM', ?, NOW(), NOW())
            ON CONFLICT (source, source_id) DO UPDATE SET
              retailer_code = EXCLUDED.retailer_code, retailer_name = EXCLUDED.retailer_name,
              branch_name = EXCLUDED.branch_name, city = EXCLUDED.city, settlement = EXCLUDED.settlement,
              locality_type = EXCLUDED.locality_type, municipality = EXCLUDED.municipality,
              region = EXCLUDED.region, address = EXCLUDED.address,
              latitude = EXCLUDED.latitude, longitude = EXCLUDED.longitude, active = TRUE,
              last_seen_at = NOW(), updated_at = NOW()
            """.trimIndent(),
            retailer.code, retailer.name, branchName, city, settlement, localityType,
            municipality, region, address, latitude, longitude, sourceId
        )
    }

    private fun matchRetailer(value: String): RetailerMatch? {
        val normalized = normalize(value)
        return retailers.firstOrNull { match -> match.aliases.any { normalized.contains(normalize(it)) } }
    }

    private fun normalize(value: String): String =
        value.lowercase().filter { it.isLetterOrDigit() }

    private data class RetailerMatch(val code: String, val name: String, val aliases: List<String>)
    private data class LocalityMatch(
        val name: String,
        val type: String,
        val municipality: String?,
        val region: String?
    )
}
