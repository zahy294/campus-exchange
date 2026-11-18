package com.campusexchange.dto;

import jakarta.validation.constraints.*;

public class RegisterForm {

    @NotBlank(message = "Enter your full name")
    @Size(min = 2, max = 100, message = "Full name must be 2 to 100 characters")
    @Pattern(regexp = ValidationRules.PERSON_NAME, message = "Use letters, spaces, dots, hyphens or apostrophes only")
    private String fullName;

    @NotBlank(message = "Enter your email address")
    @Email(message = "Enter a valid email address, like name@campus.edu")
    @Size(max = 150, message = "Email must be 150 characters or fewer")
    private String email;

    @NotBlank(message = "Choose a password")
    @Pattern(regexp = ValidationRules.PASSWORD, message = ValidationRules.PASSWORD_MESSAGE)
    private String password;

    @NotBlank(message = "Re-enter your password")
    private String confirmPassword;

    @Pattern(regexp = ValidationRules.PHONE, message = "Enter a 10-digit mobile number, e.g. 9876543210")
    private String phone;

    @Size(max = 100, message = "Department must be 100 characters or fewer")
    private String department;

    public RegisterForm() {
    }

    /** Cross-field check reported against {@code confirmPassword}. */
    @AssertTrue(message = "Passwords do not match")
    public boolean isConfirmPasswordValid() {
        // Let @NotBlank report a missing value; only flag a real mismatch.
        if (password == null || confirmPassword == null || confirmPassword.isEmpty()) {
            return true;
        }
        return password.equals(confirmPassword);
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
