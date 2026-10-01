package account_service.dto;

import account_service.model.Account;
import account_service.model.AccountStatus;
import account_service.model.Role;

import java.time.Instant;

public record AccountResponse(Long id, String firstName, String lastName, String email,
                              String phone, Role role, AccountStatus status,
                              Instant createdAt, Instant updatedAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getFirstName(), account.getLastName(),
                account.getEmail(), account.getPhone(), account.getRole(), account.getStatus(),
                account.getCreatedAt(), account.getUpdatedAt());
    }
}
