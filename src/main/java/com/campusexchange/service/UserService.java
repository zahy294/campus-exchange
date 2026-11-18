package com.campusexchange.service;

import com.campusexchange.dto.PasswordChangeForm;
import com.campusexchange.dto.ProfileForm;
import com.campusexchange.dto.RegisterForm;
import com.campusexchange.exception.DuplicateEmailException;
import com.campusexchange.exception.ResourceNotFoundException;
import com.campusexchange.model.User;
import com.campusexchange.repository.UserRepository;
import com.campusexchange.util.Text;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates an account. Emails are stored lower-cased so lookups and the
     * duplicate check are case-insensitive.
     *
     * @throws DuplicateEmailException if the email is already registered
     */
    @Transactional
    public User registerUser(RegisterForm form) {
        String email = Text.normalizeEmail(form.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = new User(
                form.getFullName().trim(),
                email,
                passwordEncoder.encode(form.getPassword()),
                Text.trimToNull(form.getPhone()),
                Text.trimToNull(form.getDepartment())
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        String normalized = Text.normalizeEmail(email);
        if (normalized == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(normalized);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public User updateProfile(String email, ProfileForm form) {
        User user = getUserByEmail(email);
        user.setFullName(form.getFullName().trim());
        user.setPhone(Text.trimToNull(form.getPhone()));
        user.setDepartment(Text.trimToNull(form.getDepartment()));
        return userRepository.save(user);
    }

    /**
     * Changes the password after verifying the current one.
     *
     * @return false if the current password was wrong (nothing is changed)
     */
    @Transactional
    public boolean changePassword(String email, PasswordChangeForm form) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);
        return true;
    }
}
