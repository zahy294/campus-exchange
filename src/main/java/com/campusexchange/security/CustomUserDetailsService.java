package com.campusexchange.security;

import com.campusexchange.model.User;
import com.campusexchange.repository.UserRepository;
import com.campusexchange.util.Text;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Emails are stored lower-case, so sign-in is case-insensitive and ignores stray spaces. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalized = Text.normalizeEmail(email);
        User user = (normalized == null ? null : userRepository.findByEmail(normalized).orElse(null));
        if (user == null) {
            throw new UsernameNotFoundException("No account for that email");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
