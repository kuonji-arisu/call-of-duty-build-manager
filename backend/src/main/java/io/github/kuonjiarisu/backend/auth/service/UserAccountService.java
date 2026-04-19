package io.github.kuonjiarisu.backend.auth.service;

import java.time.LocalDateTime;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.auth.security.AuthenticatedUser;
import io.github.kuonjiarisu.backend.mapper.UserMapper;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class UserAccountService implements UserDetailsService {

    private final UserMapper userMapper;

    public UserAccountService(UserMapper userMapper) {
        this.userMapper = userMapper;
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
}
