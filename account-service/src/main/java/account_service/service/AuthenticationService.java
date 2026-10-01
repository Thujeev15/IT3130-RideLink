package account_service.service;

import account_service.dto.LoginRequest;
import account_service.dto.LoginResponse;
import account_service.exception.InactiveAccountException;
import account_service.exception.InvalidCredentialsException;
import account_service.model.Account;
import account_service.model.AccountStatus;
import account_service.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(AccountService accountService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Account account;
        try {
            account = accountService.getByEmail(request.getEmail().trim().toLowerCase());
        } catch (RuntimeException exception) {
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (account.getStatus() != AccountStatus.ACTIVE) throw new InactiveAccountException();
        return new LoginResponse(jwtService.generateToken(account), "Bearer",
                jwtService.getExpirationSeconds(), account_service.dto.AccountResponse.from(account));
    }
}
