package bg.prices.backend.catalog

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Timestamp
import java.time.Instant

@Component
class OfficialSettlementImportJob(
    private val jdbc: JdbcTemplate,
    private val mapper: ObjectMapper
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    private val sourceUrl = "https://www.nsi.bg/nrnm/ekatte/territorial-units/json"

    override fun run(args: ApplicationArguments) {
        val existing = jdbc.queryForObject("SELECT COUNT(*) FROM official_settlements", Long::class.java) ?: 0L
        if (existing < 5000L) refreshSettlements()
    }

    @Scheduled(cron = "0 15 3 * * SUN", zone = "Europe/Sofia")
    fun scheduledRefresh() = refreshSettlements()

    fun refreshSettlements() {
        try {
            val connection = URL(sourceUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 30000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Spestovnik-Beta/1.0 (official public register import)")
            val now = Timestamp.from(Instant.now())
            val records = connection.inputStream.bufferedReader(Charsets.UTF_8).use { mapper.readTree(it) }
            val rows = mutableListOf<Array<Any?>>()
            records.forEach { row ->
                val kind = row.path("t_v_m").asText()
                val type = when {
                    kind.contains("гр") -> "CITY"
                    kind.contains("с") -> "VILLAGE"
                    else -> "OTHER"
                }
                val code = row.path("ekatte").asText()
                val name = row.path("name").asText()
                if (code.isNotBlank() && name.isNotBlank()) {
                    rows += arrayOf(code, name, row.path("name_en").asText().ifBlank { null }, type,
                        row.path("obshtina_name").asText().ifBlank { null },
                        row.path("oblast_name").asText().ifBlank { null }, sourceUrl, now)
                }
            }
            jdbc.batchUpdate(
                """
                INSERT INTO official_settlements(ekatte,name_bg,name_en,locality_type,municipality,region,source_url,source_checked_at)
                VALUES (?,?,?,?,?,?,?,?)
                ON CONFLICT (ekatte) DO UPDATE SET name_bg=EXCLUDED.name_bg,name_en=EXCLUDED.name_en,
                  locality_type=EXCLUDED.locality_type,municipality=EXCLUDED.municipality,
                  region=EXCLUDED.region,source_url=EXCLUDED.source_url,source_checked_at=EXCLUDED.source_checked_at,active=TRUE
                """.trimIndent(), rows
            )
            jdbc.update("UPDATE data_sources SET last_checked_at=? WHERE code='nsi_ekatte'", now)
            log.info("Imported {} settlements from NSI EKATTE", rows.size)
            connection.disconnect()
        } catch (ex: Exception) {
            log.warn("NSI settlement import did not complete; existing settlement data is retained: {}", ex.message)
        }
    }
}
