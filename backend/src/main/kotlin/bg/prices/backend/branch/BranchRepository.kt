package bg.prices.backend.branch

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Pageable
import java.util.UUID

interface BranchRepository : JpaRepository<Branch, UUID> {
    @Query("""
        select b from Branch b
        where b.active = true and (
          lower(b.city) = lower(:locality) or lower(coalesce(b.settlement, '')) = lower(:locality)
        )
        order by b.retailerName, b.address
    """)
    fun findByLocality(@Param("locality") locality: String): List<Branch>

    @Query("""
        select b from Branch b
        where b.active = true and (
          lower(b.city) like lower(concat('%', :query, '%')) or
          lower(coalesce(b.settlement, '')) like lower(concat('%', :query, '%')) or
          lower(coalesce(b.municipality, '')) like lower(concat('%', :query, '%')) or
          lower(coalesce(b.region, '')) like lower(concat('%', :query, '%'))
        )
        order by b.city, b.settlement, b.retailerName
    """)
    fun searchLocality(@Param("query") query: String, pageable: Pageable): List<Branch>

    @Query(value = """
        SELECT b.*,
          (6371.0088 * acos(LEAST(1.0, GREATEST(-1.0,
            cos(radians(:latitude)) * cos(radians(b.latitude)) *
            cos(radians(b.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(b.latitude))
          )))) AS distance_km
        FROM store_branches b
        WHERE b.active = true
          AND (6371.0088 * acos(LEAST(1.0, GREATEST(-1.0,
            cos(radians(:latitude)) * cos(radians(b.latitude)) *
            cos(radians(b.longitude) - radians(:longitude)) +
            sin(radians(:latitude)) * sin(radians(b.latitude))
          )))) <= :radiusKm
        ORDER BY distance_km ASC
        LIMIT 200
    """, nativeQuery = true)
    fun findNearby(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusKm") radiusKm: Double
    ): List<Array<Any>>
}
