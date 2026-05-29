package com.ticketsystem.security;

import com.ticketsystem.core.exception.ResourceNotFoundException;
import com.ticketsystem.entity.User;
import com.ticketsystem.service.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Spring Security kimlik doğrulaması için kullanıcı yükleme işlemlerini yönetir. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userService.getUserByEmail(username);

            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.isActive(),
                    true,
                    true,
                    true,
                    authorities
            );
        } catch (ResourceNotFoundException ex) {
            throw new UsernameNotFoundException("Kullanıcı bulunamadı.", ex);
        }
    }
}
