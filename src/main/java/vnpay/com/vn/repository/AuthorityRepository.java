package vnpay.com.vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vnpay.com.vn.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
