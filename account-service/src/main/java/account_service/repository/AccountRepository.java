package account_service.repository;

import account_service.model.Account;
import account_service.model.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmailIgnoreCase(String email);
    Optional<Account> findByIdAndStatus(Long id, AccountStatus status);
    boolean existsByEmailIgnoreCase(String email);
}
