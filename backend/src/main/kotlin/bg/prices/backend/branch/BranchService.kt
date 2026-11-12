package bg.prices.backend.branch

import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@Service
class BranchService(private val repository: BranchRepository) {
    @Transactional(readOnly = true)
    fun inLocality(locality: String?): List<BranchResponse> {
        val name = locality?.trim().orEmpty()
        if (name.isBlank()) return emptyList()
        return repository.findByLocality(name).take(200).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun searchLocalities(query: String): List<LocalitySuggestion> {
        val term = query.trim()
        if (term.length < 2) return emptyList()
        return repository.searchLocality(term, PageRequest.of(0, 50))
            .distinctBy { listOf(it.city.lowercase(), it.settlement?.lowercase(), it.localityType) }
            .take(30)
            .map {
                LocalitySuggestion(
                    name = it.settlement ?: it.city,
                    type = it.localityType,
                    municipality = it.municipality,
                    region = it.region
                )
            }
    }

    @Transactional(readOnly = true)
    fun nearby(latitude: Double, longitude: Double, radiusKm: Double): List<BranchResponse> {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
        require(radiusKm in 1.0..200.0) { "Radius must be between 1 and 200 km." }
        return repository.findNearby(latitude, longitude, radiusKm).map { row ->
            val branch = Branch(
                id = row[0] as java.util.UUID,
                retailerCode = row[1] as String,
                retailerName = row[2] as String,
                branchName = row[3] as String?,
                city = row[4] as String,
                settlement = row[5] as String?,
                localityType = row[6] as String,
                municipality = row[7] as String?,
                region = row[8] as String?,
                address = row[9] as String,
                latitude = (row[10] as Number).toDouble(),
                longitude = (row[11] as Number).toDouble(),
                active = row[12] as Boolean
            )
            branch.toResponse((row.last() as Number).toDouble())
        }
    }
}

data class LocalitySuggestion(
    val name: String,
    val type: String,
    val municipality: String?,
    val region: String?
)
