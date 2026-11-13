package bg.prices.backend.branch

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "store_branches")
class Branch(
    @Id
    @GeneratedValue
    var id: UUID? = null,
    @Column(name = "retailer_code", nullable = false, length = 64)
    var retailerCode: String = "",
    @Column(name = "retailer_name", nullable = false, length = 160)
    var retailerName: String = "",
    @Column(name = "branch_name", length = 160)
    var branchName: String? = null,
    @Column(nullable = false, length = 120)
    var city: String = "",
    @Column(length = 160)
    var settlement: String? = null,
    @Column(name = "locality_type", nullable = false, length = 24)
    var localityType: String = "OTHER",
    @Column(length = 120)
    var municipality: String? = null,
    @Column(length = 120)
    var region: String? = null,
    @Column(name = "postal_code", length = 4)
    var postalCode: String? = null,
    @Column(nullable = false, length = 300)
    var address: String = "",
    @Column(nullable = false)
    var latitude: Double = 0.0,
    @Column(nullable = false)
    var longitude: Double = 0.0,
    @Column(name = "source_type", length = 24)
    var sourceType: String? = null,
    @Column(name = "source_url", length = 1000)
    var sourceUrl: String? = null,
    @Column(name = "source_verified_at")
    var sourceVerifiedAt: Instant? = null,
    @Column(nullable = false)
    var active: Boolean = true,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)

data class BranchResponse(
    val id: UUID,
    val retailerCode: String,
    val retailerName: String,
    val branchName: String?,
    val city: String,
    val settlement: String?,
    val localityType: String,
    val municipality: String?,
    val region: String?,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double?
)

fun Branch.toResponse(distanceKm: Double? = null) = BranchResponse(
    id = requireNotNull(id), retailerCode = retailerCode, retailerName = retailerName,
    branchName = branchName, city = city, settlement = settlement, localityType = localityType,
    municipality = municipality, region = region, address = address,
    latitude = latitude, longitude = longitude, distanceKm = distanceKm
)
