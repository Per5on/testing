package bg.prices.backend.submission

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/submissions")
class SubmissionController(private val jdbc: JdbcTemplate) {
    @PostMapping
    fun submit(@Valid @RequestBody request: SubmissionRequest): SubmissionAccepted {
        val type = request.type.trim().uppercase()
        if (type !in setOf("SUGGESTION", "NOTE", "OPINION")) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported submission type.")
        }
        if ((request.latitude == null) != (request.longitude == null) ||
            request.latitude?.let { it !in -90.0..90.0 } == true ||
            request.longitude?.let { it !in -180.0..180.0 } == true
        ) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location coordinates.")
        }
        val id = jdbc.queryForObject(
            """
            INSERT INTO user_submissions(submission_type, content, location_label, latitude, longitude)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """.trimIndent(),
            UUID::class.java,
            type, request.content.trim(), request.locationLabel?.trim()?.take(180),
            request.latitude, request.longitude
        ) ?: throw IllegalStateException("Submission was not stored.")
        return SubmissionAccepted(id.toString(), "PENDING")
    }
}

data class SubmissionRequest(
    @field:NotBlank
    @field:Size(max = 24)
    val type: String,
    @field:NotBlank
    @field:Size(max = 4000)
    val content: String,
    @field:Size(max = 180)
    val locationLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class SubmissionAccepted(val id: String, val status: String)
