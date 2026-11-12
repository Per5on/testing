package bg.prices.backend.config

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

@Component
class ApiRateLimitFilter : OncePerRequestFilter() {
    private data class Window(var startedAt: Long, var count: Int) {
        @Synchronized
        fun allow(now: Long, limit: Int, durationMs: Long): Boolean {
            if (now - startedAt >= durationMs) {
                startedAt = now
                count = 0
            }
            count += 1
            return count <= limit
        }
    }

    private val windows = Caffeine.newBuilder()
        .maximumSize(20_000)
        .expireAfterAccess(Duration.ofMinutes(2))
        .build<String, Window>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("X-Frame-Options", "DENY")
        response.setHeader("Referrer-Policy", "no-referrer")
        response.setHeader("Permissions-Policy", "camera=(), microphone=()")
        if (!request.requestURI.startsWith("/api/v1/")) {
            filterChain.doFilter(request, response)
            return
        }

        val submissions = request.requestURI == "/api/v1/submissions" && request.method == "POST"
        val limit = if (submissions) 5 else 120
        val windowMs = 60_000L
        val key = "${request.remoteAddr}:${if (submissions) "submissions" else "api"}"
        val now = System.currentTimeMillis()
        val allowed = windows.get(key) { Window(now, 0) }.allow(now, limit, windowMs)
        if (!allowed) {
            response.status = 429
            response.setHeader("Retry-After", "60")
            response.contentType = "application/json"
            response.writer.write("""{"error":"rate_limit_exceeded","message":"Too many requests. Please try again shortly."}""")
            return
        }
        filterChain.doFilter(request, response)
    }
}
