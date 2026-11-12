package bg.prices.backend.locality

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface LocalityRepository : JpaRepository<InhabitedLocality, UUID> {
    @Query("""
        select l from InhabitedLocality l
        where l.onMap = true and (
          lower(l.name) like lower(concat('%', :query, '%')) or
          lower(coalesce(l.nameBg, '')) like lower(concat('%', :query, '%'))
        )
        order by l.name
    """)
    fun searchOnMap(@Param("query") query: String): List<InhabitedLocality>
}
