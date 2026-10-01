package account_service.controller;

import account_service.dto.AccountResponse;
import account_service.dto.LoginRequest;
import account_service.dto.LoginResponse;
import account_service.dto.RegisterRequest;
import account_service.dto.UpdateProfileRequest;
import account_service.service.AccountService;
import account_service.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AuthenticationService authenticationService;

    public AccountController(AccountService accountService, AuthenticationService authenticationService) {
        this.accountService = accountService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register an account", description = "Creates an active passenger or driver account.")
    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.register(request));
    }

    @Operation(summary = "Login", description = "Authenticates an active account and returns a signed JWT.")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }

    @Operation(summary = "Get my profile", security = @SecurityRequirement(name = "bearerAuth"))
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public AccountResponse me(Authentication authentication) {
        return accountService.getProfile(Long.valueOf(authentication.getName()));
    }

    @Operation(summary = "Update my profile", security = @SecurityRequirement(name = "bearerAuth"))
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public AccountResponse update(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return accountService.updateProfile(Long.valueOf(authentication.getName()), request);
    }

    @GetMapping("/passenger")
    @PreAuthorize("hasRole('PASSENGER')")
    public AccountResponse passenger(Authentication authentication) { return me(authentication); }

    @GetMapping("/driver")
    @PreAuthorize("hasRole('DRIVER')")
    public AccountResponse driver(Authentication authentication) { return me(authentication); }
}
