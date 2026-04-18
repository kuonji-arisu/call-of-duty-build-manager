package io.github.kuonjiarisu.backend.auth.service;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.auth.config.AuthProperties;
import io.github.kuonjiarisu.backend.auth.model.UserAccount;
import io.github.kuonjiarisu.backend.auth.model.UserRole;
import io.github.kuonjiarisu.backend.auth.security.AuthenticatedUser;
import io.github.kuonjiarisu.backend.mapper.UserMapper;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class UserAccountService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    public UserAccountService(
        UserMapper userMapper,
        PasswordEncoder passwordEncoder,
        AuthProperties authProperties
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authProperties = authProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userMapper.findByUsername(DomainSupport.requireText(username, "用户名"));
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return new AuthenticatedUser(user);
    }

    public AuthenticatedUser loadAuthenticatedUserById(String userId) {
        var user = userMapper.findById(DomainSupport.requireText(userId, "用户 ID"));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new AuthenticatedUser(user);
    }

    public void markLoggedIn(String userId) {
        userMapper.updateLastLoginAt(userId, LocalDateTime.now());
    }

    @Bean
    public ApplicationRunner defaultAdminInitializer() {
        return args -> {
            if (userMapper.countAll() > 0) {
                log.info("Default admin initializer skipped because users already exist");
                return;
            }

            var now = LocalDateTime.now();
            var defaultAdmin = new UserAccount(
                DomainSupport.keepOrGenerateId(null, "user"),
                authProperties.defaultAdminUsername(),
                passwordEncoder.encode(authProperties.defaultAdminPassword()),
                UserRole.ADMIN.name(),
                authProperties.defaultAdminDisplayName(),
                true,
                null,
                now,
                now
            );
            userMapper.insert(defaultAdmin);
            log.info("Default admin user initialized: username={}", defaultAdmin.username());
        };
    }
}
