package account_service.controller;

import account_service.dto.AccountResponse;
import account_service.dto.RegisterRequest;
import account_service.model.AccountStatus;
import account_service.model.Role;
import account_service.service.AccountService;
import account_service.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {
    @Mock AccountService accountService;
    @Mock AuthenticationService authenticationService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AccountController(accountService, authenticationService)).build();
    }

    @Test
    void registerReturnsCreatedProfileWithoutPassword() throws Exception {
        when(accountService.register(any(RegisterRequest.class))).thenReturn(profile());

        mockMvc.perform(post("/api/accounts/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rider@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void invalidRegistrationReturnsBadRequest() throws Exception {
        RegisterRequest request = validRequest();
        request.setEmail("not-an-email");

        mockMvc.perform(post("/api/accounts/register").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private RegisterRequest validRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Rider"); request.setLastName("One"); request.setEmail("rider@example.com");
        request.setPassword("StrongPass1!"); request.setPhone("+94771234567"); request.setRole(Role.PASSENGER);
        return request;
    }

    private AccountResponse profile() {
        Instant now = Instant.now();
        return new AccountResponse(1L, "Rider", "One", "rider@example.com", "+94771234567",
                Role.PASSENGER, AccountStatus.ACTIVE, now, now);
    }
}
