package io.github.kuonjiarisu.backend.auth.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.github.kuonjiarisu.backend.auth.model.AuthUserView;
import io.github.kuonjiarisu.backend.auth.model.UserAccount;

public class AuthenticatedUser implements UserDetails {

    private final UserAccount user;

    public AuthenticatedUser(UserAccount user) {
        this.user = user;
    }

    public String id() {
        return user.id();
    }

    public String role() {
        return user.role();
    }

    public String displayName() {
        return user.displayName();
    }

    public AuthUserView toView() {
        return new AuthUserView(user.id(), user.username(), user.displayName(), user.role());
    }

    public UserAccount userAccount() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.role()));
    }

    @Override
    public String getPassword() {
        return user.passwordHash();
    }

    @Override
    public String getUsername() {
        return user.username();
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.enabled());
    }
}
