package com.campusexchange.dto;

import com.campusexchange.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Editable account details on the profile page. Email is the login id and is not editable. */
public class ProfileForm {

    @NotBlank(message = "Enter your full name")
    @Size(min = 2, max = 100, message = "Full name must be 2 to 100 characters")
    @Pattern(regexp = ValidationRules.PERSON_NAME, message = "Use letters, spaces, dots, hyphens or apostrophes only")
    private String fullName;

    @Pattern(regexp = ValidationRules.PHONE, message = "Enter a 10-digit mobile number, e.g. 9876543210")
    private String phone;

    @Size(max = 100, message = "Department must be 100 characters or fewer")
    private String department;

    public static ProfileForm from(User user) {
        ProfileForm form = new ProfileForm();
        form.setFullName(user.getFullName());
        form.setPhone(user.getPhone());
        form.setDepartment(user.getDepartment());
        return form;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
