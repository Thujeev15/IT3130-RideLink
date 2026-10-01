package account_service.dto;

import account_service.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @NotBlank @Email @Size(max = 254) private String email;
    @NotBlank @Size(min = 8, max = 72)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$", message = "must contain upper, lower, digit, and special character")
    private String password;
    @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "must be a valid phone number") private String phone;
    @NotNull private Role role;

    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { firstName = value; }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { lastName = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { password = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public Role getRole() { return role; }
    public void setRole(Role value) { role = value; }
}
