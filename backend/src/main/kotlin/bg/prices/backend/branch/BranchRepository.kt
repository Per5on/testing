package bg.prices.backend.branch

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BranchRepository : JpaRepository<Branch, UUID> {
    @Query("""
        select b from Branch b
        where b.active = true
          and b.sourceType in ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
          and b.sourceUrl is not null
          and b.sourceVerifiedAt is not null
          and (
            lower(b.city) = lower(:locality)
            or lower(coalesce(b.settlement, '')) = lower(:locality)
          )
        order by b.retailerName, b.address
    """)
    fun findByLocality(@Param("locality") locality: String, pageable: Pageable): List<Branch>

    @Query("""
        select b from Branch b
        where b.active = true
          and b.sourceType in ('OFFICIAL_WEBSITE', 'OFFICIAL_APP')
          and b.sourceUrl is not null
          and b.sourceVerifiedAt is not null
          and b.postalCode = :postalCode
        order by b.retailerName, b.city, b.address
    """)
    fun findByPostalCode(@Param("postalCode") postalCode: String, pageable: Pageable): List<Branch>
}
