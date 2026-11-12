package bg.prices.backend.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class CacheConfiguration {
    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager("catalog", "localities").apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .maximumSize(500)
                    .expireAfterWrite(Duration.ofMinutes(5))
            )
        }
}
