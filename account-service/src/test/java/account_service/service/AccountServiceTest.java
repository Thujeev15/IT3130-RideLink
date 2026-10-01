package account_service.service;

import account_service.dto.RegisterRequest;
import account_service.exception.DuplicateEmailException;
import account_service.model.Account;
import account_service.model.Role;
import account_service.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AccountService accountService;

    @Test
    void registerHashesPasswordAndDefaultsAccountToActive() {
        RegisterRequest request = request();
        when(accountRepository.existsByEmailIgnoreCase("rider@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass1!")).thenReturn("bcrypt-hash");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });

        var response = accountService.register(request);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals("bcrypt-hash", captor.getValue().getPassword());
        assertEquals("rider@example.com", captor.getValue().getEmail());
        assertEquals("PASSENGER", response.role().name());
        assertEquals("ACTIVE", response.status().name());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(accountRepository.existsByEmailIgnoreCase("rider@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> accountService.register(request()));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void registerAllowsAdminRole() {
        RegisterRequest request = request();
        request.setRole(Role.ADMIN);
        when(accountRepository.existsByEmailIgnoreCase("rider@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass1!")).thenReturn("bcrypt-hash");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            account.setId(2L);
            return account;
        });

        var response = accountService.register(request);

        assertEquals(Role.ADMIN, response.role());
    }

    private RegisterRequest request() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Rider");
        request.setLastName("One");
        request.setEmail(" Rider@example.com ");
        request.setPassword("StrongPass1!");
        request.setPhone("+94771234567");
        request.setRole(Role.PASSENGER);
        return request;
    }
}
