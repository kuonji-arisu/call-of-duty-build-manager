package io.github.kuonjiarisu.backend.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import io.github.kuonjiarisu.backend.auth.security.AuthenticatedUser;
import io.github.kuonjiarisu.backend.auth.security.JwtTokenService;
import io.github.kuonjiarisu.backend.auth.model.CaptchaResponse;
import io.github.kuonjiarisu.backend.auth.model.LoginRequest;
import io.github.kuonjiarisu.backend.auth.model.LoginResponse;
import io.github.kuonjiarisu.backend.auth.model.LogoutResponse;
import io.github.kuonjiarisu.backend.support.DomainSupport;

@Service
public class AuthService {

    private final CaptchaService captchaService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserAccountService userAccountService;

    public AuthService(
        CaptchaService captchaService,
        AuthenticationManager authenticationManager,
        JwtTokenService jwtTokenService,
        UserAccountService userAccountService
    ) {
        this.captchaService = captchaService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userAccountService = userAccountService;
    }

    public CaptchaResponse createCaptcha() {
        return captchaService.createCaptcha();
    }

    public LoginResponse login(LoginRequest request) {
        captchaService.validateAndConsume(request.captchaId(), request.captchaCode());

        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                DomainSupport.requireText(request.username(), "用户名"),
                DomainSupport.requireText(request.password(), "密码")
            )
        );

        var user = (AuthenticatedUser) authentication.getPrincipal();
        userAccountService.markLoggedIn(user.id());

        return new LoginResponse(
            jwtTokenService.generateAccessToken(user.userAccount()),
            "Bearer",
            jwtTokenService.accessTokenTtlSeconds(),
            user.toView()
        );
    }

    public LogoutResponse logout() {
        return new LogoutResponse("已退出登录");
    }
}
