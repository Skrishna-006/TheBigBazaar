package com.siva.shopsphere.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.siva.shopsphere.accounts.entity.User;
import com.siva.shopsphere.accounts.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByPhoneNumber(username)
            .orElseGet(() -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found")));
        return new org.springframework.security.core.userdetails.User(
            user.getPhoneNumber() != null ? user.getPhoneNumber() : user.getEmail(),
            user.getPasswordHash(),
            user.isActive(),
            true,
            true,
            true,
            authorities(user.getRole().name())
        );
    }

    private List<GrantedAuthority> authorities(String role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
