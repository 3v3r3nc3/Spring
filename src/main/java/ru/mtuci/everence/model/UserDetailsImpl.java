package ru.mtuci.everence.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

@Data
public class UserDetailsImpl implements UserDetails {
    private String username;
    private String password;
    private Set<GrantedAuthority> authorities;
    private boolean active;

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public static UserDetails fromApplicationUser(ApplicationUser user) {
        return new User(
                user.getEmail(),
                user.getPassword(),
                user.getRole().getGrantedAuthorities()
        );
    }
}
