package bg.prices.backend.locality

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "inhabited_localities")
class InhabitedLocality(
    @Id @GeneratedValue
    var id: UUID? = null,
    @Column(name = "source", nullable = false, length = 40)
    var source: String = "OSM",
    @Column(name = "source_id", nullable = false, length = 100)
    var sourceId: String = "",
    @Column(nullable = false, length = 180)
    var name: String = "",
    @Column(name = "name_bg", length = 180)
    var nameBg: String? = null,
    @Column(nullable = false, length = 24)
    var type: String = "OTHER",
    @Column(length = 160)
    var municipality: String? = null,
    @Column(length = 160)
    var region: String? = null,
    @Column(nullable = false)
    var latitude: Double = 0.0,
    @Column(nullable = false)
    var longitude: Double = 0.0,
    @Column(name = "on_map", nullable = false)
    var onMap: Boolean = true
)

data class LocalityResponse(
    val id: UUID,
    val name: String,
    val nameBg: String?,
    val type: String,
    val municipality: String?,
    val region: String?,
    val latitude: Double,
    val longitude: Double,
    val source: String
)

fun InhabitedLocality.toResponse() = LocalityResponse(
    id = requireNotNull(id), name = name, nameBg = nameBg, type = type,
    municipality = municipality, region = region, latitude = latitude,
    longitude = longitude, source = source
)
