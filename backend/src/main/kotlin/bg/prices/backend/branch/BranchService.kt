package bg.prices.backend.branch

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BranchService(private val repository: BranchRepository) {
    @Transactional(readOnly = true)
    fun inLocality(locality: String?): List<BranchResponse> {
        val name = locality?.trim().orEmpty()
        if (name.isBlank()) return emptyList()
        return repository.findByLocality(name, PageRequest.of(0, 500)).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun inPostalCode(postalCode: String?): List<BranchResponse> {
        val code = postalCode?.trim().orEmpty()
        if (!code.matches(Regex("^[0-9]{4}$"))) return emptyList()
        return repository.findByPostalCode(code, PageRequest.of(0, 500)).map { it.toResponse() }
    }
}
