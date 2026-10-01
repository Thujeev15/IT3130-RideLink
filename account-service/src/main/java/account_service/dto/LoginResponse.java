package account_service.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn,
                            AccountResponse account) {
}