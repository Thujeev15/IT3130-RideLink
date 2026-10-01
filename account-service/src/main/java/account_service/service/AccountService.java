package account_service.service;

import account_service.dto.AccountResponse;
import account_service.dto.RegisterRequest;
import account_service.dto.UpdateProfileRequest;
import account_service.exception.AccountNotFoundException;
import account_service.exception.DuplicateEmailException;
import account_service.model.Account;
import account_service.model.AccountStatus;
import account_service.repository.AccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (accountRepository.existsByEmailIgnoreCase(email)) throw new DuplicateEmailException();
        Account account = new Account();
        account.setFirstName(request.getFirstName().trim());
        account.setLastName(request.getLastName().trim());
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setPhone(request.getPhone());
        account.setRole(request.getRole());
        account.setStatus(AccountStatus.ACTIVE);
        try {
            return AccountResponse.from(accountRepository.save(account));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public Account getByEmail(String email) {
        return accountRepository.findByEmailIgnoreCase(email).orElseThrow(AccountNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public AccountResponse getProfile(Long accountId) {
        return AccountResponse.from(getAccount(accountId));
    }

    @Transactional
    public AccountResponse updateProfile(Long accountId, UpdateProfileRequest request) {
        Account account = getAccount(accountId);
        account.setFirstName(request.getFirstName().trim());
        account.setLastName(request.getLastName().trim());
        account.setPhone(request.getPhone());
        return AccountResponse.from(accountRepository.save(account));
    }

    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
    }
}
