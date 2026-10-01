package account_service.security;

import account_service.model.Account;
import account_service.model.AccountStatus;
import account_service.model.Role;
import account_service.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class SecurityAuthorizationTest {
    @Autowired WebApplicationContext webApplicationContext;
    @Autowired AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;
    private Account passenger;
    private Account driver;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
            .apply(springSecurity(springSecurityFilterChain)).build();
        accountRepository.deleteAll();
        passenger = saveAccount("passenger@example.com", Role.PASSENGER);
        driver = saveAccount("driver@example.com", Role.DRIVER);
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/accounts/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void passengerCanAccessPassengerEndpoint() throws Exception {
        mockMvc.perform(get("/api/accounts/passenger")
                        .header("Authorization", bearerToken(passenger)))
                .andExpect(status().isOk());
    }

    @Test
    void driverCanAccessDriverEndpoint() throws Exception {
        mockMvc.perform(get("/api/accounts/driver")
                        .header("Authorization", bearerToken(driver)))
                .andExpect(status().isOk());
    }

    @Test
    void passengerCannotAccessDriverEndpoint() throws Exception {
        mockMvc.perform(get("/api/accounts/driver")
                        .header("Authorization", bearerToken(passenger)))
                .andExpect(status().isForbidden());
    }

    private Account saveAccount(String email, Role role) {
        Account account = new Account();
        account.setFirstName(role.name());
        account.setLastName("Test");
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode("StrongPass1!"));
        account.setPhone("+94771234567");
        account.setRole(role);
        account.setStatus(AccountStatus.ACTIVE);
        return accountRepository.save(account);
    }

    private String bearerToken(Account account) {
        return "Bearer " + jwtService.generateToken(account);
    }
}
