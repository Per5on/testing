package bg.prices.backend.catalog

import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("osm-import")
class OsmImportRunner {
    @Bean
    fun runOsmImport(importer: OpenStreetMapImportJob) = ApplicationRunner {
        importer.importOnce()
    }
}
